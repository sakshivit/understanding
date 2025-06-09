package org.example.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.function.Constant;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chubb.na.domain.data.message.DocumentIngestion;
import com.chubb.na.domain.data.message.SourceContext;
import com.chubb.na.domain.data.product.dto.EnrichedFields;
import com.chubb.na.domain.data.product.restclient.impl.AdlsAPIAccessor;
import com.chubb.na.domain.helper.FeatureFlagHelper;
import com.chubb.na.domain.helper.Helper;
import com.chubb.na.domain.helper.JsonHelper;
import com.chubb.na.domain.kafka.producer.KafkaDomainProducer;
import com.chubb.na.domain.utils.Commons;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

@ApplicationScoped
public class ProcessSubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessSubmissionService.class);

    @ConfigProperty(name = "orchestration.topic")
    String orchestrationTopic;

    @ConfigProperty(name = "sim.topic")
    String simTopic;

    @ConfigProperty(name = "async.lossrun.quename")
    String asyncLossRunQueueName;

    @ConfigProperty(name = "async.lossrun.topic")
    String asyncLossRunTopic;

    @Inject
    Jsonb jsonb;

    @Inject
    AdlsAPIAccessor adlsAPIAccessor;

    @Inject
    KafkaDomainProducer kafkaPublisher;

    @Inject
    JsonHelper jsonHelper;

    @Inject
    Helper helper;

    @Inject
    FeatureFlagHelper featureFlagHelper;

    @Inject
    UpdateSimStatusService updateSimStatusService;

    public void processSubmission(String intakeMessage, String lob) {
        logger.info("Inside processSubmission, IntakeMessage:-" + intakeMessage);
        if(isAsyncLossRun(intakeMessage)){
            logger.info("Message forwarded to AsyncLossRun Topic message = {}", intakeMessage);
            kafkaPublisher.publishMessage(intakeMessage, asyncLossRunTopic);
            return;
        }
        JSONObject message = new JSONObject(intakeMessage);
        String submissionNumber = Commons.EMPTY_STRING;
        boolean kickoutCisaManual = false;
        try {
            boolean skipSTP = featureFlagHelper.getFeatureFlagAsBoolean(Commons.SKIP_STP_FLAG);

            boolean skipCISA = featureFlagHelper.getFeatureFlagAsBoolean(Commons.SKIP_CISA_FLAG);

            String currentStage = helper.getStageCode(message);
            message = helper.setInprogressStatus(message);
            submissionNumber = jsonHelper.getSubmissionNumberFromJson(message);
            logger.info("-------------SUBMISSION NUMBER-----" + submissionNumber);
            JSONArray documents = jsonHelper.getDocumentsInfoFromJson(message);
            String queue = helper.getQueueName(message);
            logger.info("-------------QUEUE NAME-----" + queue);
            String priority = "Standard";
            if(queue.toLowerCase().contains("rush")){
                priority = "Rush";
            }
            message.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).putOpt(Commons.PRIORITY, priority);
            logger.info("-------------PRIORITY SET TO -----" + priority);

            for (int i = 0; i < documents.length(); i++) {
                if (currentStage.equalsIgnoreCase(Commons.GOLD_DATA_COMPLETE_STAGE)){
                    logger.info("-------------GOLD DATA READY FOR SUBMISSION NUMBER-----" + submissionNumber);
                    String documentUri = documents.getJSONObject(i).optString(Commons.URI_FIELD);
                    logger.info("--------URI--------" + documentUri);
                    //Download GOLD DATA JSON File
                    String standardizedJson = adlsAPIAccessor.downloadFrmAdls(documentUri);
                    logger.info("-------GOLD DATA FILE HAS BEEN DOWNLOADED--------------");
                    String response=helper.submitToDownstreamSystem(standardizedJson, lob, queue, message);
                    logger.info("---------------------RESPONSE FROM DOWNSTREAM SYSTEM: " + response);

                    if (response.contains(Commons.SUBMITTED_TO_DOWNSTREAM_SYSTEM)) {
                        logger.info("---------------------SUCCESSFULLY SUBMITTED TO DOWNSTREAM API-----------------------------------------");
                        message = helper.sendSuccessMessageToOrchestration(message, i);
                        String simProduct = jsonHelper.getSimProduct(standardizedJson);
                        String marketSegment = jsonHelper.getMarketSegment(standardizedJson);
                        String transaction = jsonHelper.getTransaction(standardizedJson);
                        String simMsg = helper.preparePendingMessage(message);
                        kafkaPublisher.publishMessage(simMsg, this.simTopic);
                        updateSimStatusService.updateIncidentDetails(message,simProduct,transaction,marketSegment);
                    } else {
                        logger.error("---------------------FAILED TO SUBMIT TO DOWNSTREAM API-----------------------------------------");
                        message = helper.sendErrorMessageToOrchestration(message, i);
                    }

                }
                else {
                    //for (int i = 0; i < documents.length(); i++) {
                    String documentUri = documents.getJSONObject(i).optString(Commons.URI_FIELD);
                    logger.info("--------URI--------" + documentUri);
                    //Download JSON File
                    String standardizedJson = adlsAPIAccessor.downloadFrmAdls(documentUri);
                    logger.info("-------FILE HAS BEEN DOWNLOADED--------------");

                    // check if standardizedJson is null, if so then skip the loop and log the message
                    if (standardizedJson == null || standardizedJson.isEmpty()) {
                        logger.warn("standardizedJson is null or empty, cannot process the document : ");
                        continue;

                    }

                    boolean isBrokerAPISubmission = helper.checkForBrokerAPISubmission(message);

                    // D8INGEST-6939 Check and Update CISA Identifier
                    String cisaIdentifier = skipCISA ? Commons.N : jsonHelper.deriveCISAIdentifier(standardizedJson, intakeMessage);
                    standardizedJson = jsonHelper.updateCISAIdentifier(standardizedJson, intakeMessage, cisaIdentifier);
                    if(!cisaIdentifier.equalsIgnoreCase(Commons.Y) && Commons.cisaQueueMapping.containsKey(queue)){
                        kickoutCisaManual = true;
                        String kickoutManualQueue = Commons.cisaQueueMapping.get(queue);
                        logger.info("---------------- SUBMISSION {} KICKING OUT FROM {} TO {} --------------------", submissionNumber, queue, kickoutManualQueue);
                        updateSimStatusService.updateIncidentQueue(message, kickoutManualQueue);
                        updateSimStatusService.updateIncidentStatus(message, Commons.NEW_SIM_STATUS);
                        return;
                    }

                    String peIdentifier = helper.checkIdentifiers(jsonHelper.getIncidentFromKafkaMessage(message), submissionNumber);
                    standardizedJson = helper.updatePEIdentifier(standardizedJson, peIdentifier.equalsIgnoreCase(Commons.PE_IDENTIFIER)? Commons.Y : Commons.N);

                    //String lmmIdentifier = helper.checkIdentifiers(jsonHelper.getIncidentFromKafkaMessage(message), submissionNumber);
                    standardizedJson = helper.updateLMMIdentifier(standardizedJson, peIdentifier.equalsIgnoreCase(Commons.LMM_IDENTIFIER)? Commons.Y : Commons.N);

                    //Get Common Derived Fields Response
                    String derivedFieldsResponse = helper.getCommonDerivedFieldsResponse(standardizedJson);
                    standardizedJson = StringUtils.isNotBlank(derivedFieldsResponse) ? derivedFieldsResponse : standardizedJson;

                    standardizedJson = helper.validateProducerState(standardizedJson);

                    //Get Enrichment response
                    EnrichedFields enrichedFields = helper.getEnrichmentInfo(standardizedJson);
                    String enrichedJson = jsonb.toJson(enrichedFields);
                    logger.info("---------ENRICHED JSON --------------" + enrichedJson);

                    //Append Enriched JSON to Standardized JSON for RC and FL
                    standardizedJson = jsonHelper.appendEnrichedDataInJson(standardizedJson, enrichedJson, message.toString());

                    // Compute common RC and CIB fields
                    standardizedJson = helper.enrichCommonFields(standardizedJson,submissionNumber);

                    //Get Derived Fields from RC or FL
                    String RCorFLResponse = helper.getDerivationResponse(standardizedJson, lob);
                    standardizedJson = StringUtils.isNotBlank(RCorFLResponse) ? RCorFLResponse : standardizedJson;

                    //Remove Enriched Data from Json
                    standardizedJson = jsonHelper.removeEnrichedDataFromJson(standardizedJson);

                    //Fill the Enriched Info in respective fields in Standardized JSON
                    String finalJson = jsonHelper.updateEnrichmentFieldsInIngestionData(standardizedJson, enrichedJson, lob, message, enrichedFields);

                    // Check if the account is major and if so update the message to skip stp
                    if (!skipSTP) { // if skipSTP == false
                        skipSTP = helper.checkForUserReviewNeeded(finalJson);
                    }

                    if(isBrokerAPISubmission) {
                        logger.info("----- SUBMISSION : "+submissionNumber+" IS A BROKER API SUBMISSION -----");
                        finalJson = helper.getBrokerAPIResponse(finalJson);
                    }

                    // Derive CISA specific fields
                    if(cisaIdentifier.equalsIgnoreCase(Commons.Y)){
                        finalJson = helper.deriveCISAFields(finalJson);
                    }

                    message = helper.enrichAccountInfo(finalJson, message);

                    //Do the clearance Check
                    String clearanceStatusJson = helper.performClearanceCheck(finalJson, lob);
                    if (clearanceStatusJson.equalsIgnoreCase(Commons.TECH_EXCEPTION_FIELD)){
                        logger.info("----FOR SUBMISSION: "+submissionNumber+" COULD NOT CONNECT TO CLEARANCE CHECK API FOR BLOCK/DUPLICATE CHECK----");
                        message.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.MESSAGE_FIELD, "Clearance Check Failed");
                        message = helper.uploadJsonToADLS(finalJson, message, i);
                    } else if(StringUtils.isBlank(clearanceStatusJson)){
                        //Upload JSON to ADLS as CLEARNACE CHECK CLEARED
                        logger.info("-------ALL POLICIES WERE CLEARED FOR THE SUBMISSION: "+submissionNumber+"--------------");
                        message = helper.uploadJsonToADLS(finalJson, message, i);
                    } else {
                        logger.info("-------SOME POLICIES WERE NOT CLEARED FOR THE SUBMISSION: "+submissionNumber+" LOGGING EXCEPTION--------------");
                        String msg = "All policies were not cleared. Some were Block/Duplicate : " + clearanceStatusJson;
                        finalJson = jsonHelper.removeCoverageStatusField(finalJson);
                        message.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.MESSAGE_FIELD, msg);
                        message = helper.uploadJsonToADLS(finalJson, message, i);
                    }

                    if(skipSTP){
                        message.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STP, Commons.SKIP_STP);
                    }

                    //}
                }
                break;
            }
        } catch (Exception e) {
            logger.error("ERROR while Processing Submission:- "+e.getLocalizedMessage());
            message.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.EVENT_TYPE_FIELD,
                    Commons.TECH_EXCEPTION_FIELD);
            message.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD, Commons.FAILED_STATUS);
        } finally {
            if(!kickoutCisaManual) {
                kafkaPublisher.publishMessage(message.toString(), this.orchestrationTopic);
                logger.info("-----Message Has Been Published to docai_orchestration. SUBMISSION NUMBER------" + submissionNumber);
            }
        }
    }

    private boolean isAsyncLossRun(String intakeMessage){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DocumentIngestion documentIngestion = null;

        try {
            documentIngestion = objectMapper.readValue(intakeMessage, DocumentIngestion.class);
        } catch (JsonProcessingException e) {
            logger.error("Error in parsing kafka message : {}", intakeMessage, e);
            return false;
        }

        // Get queue_name from source_context
        SourceContext sourceContext = documentIngestion.getSourceContext();

        String queueName = null;
        if(null != sourceContext){
            queueName = sourceContext.getQueueName();
        }


        if(StringUtils.isNotBlank(queueName) && queueName.trim().equalsIgnoreCase(asyncLossRunQueueName))
            return true;

        return false;
    }
}
