package org.example.helper;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chubb.na.domain.data.product.dto.AccountName;
import com.chubb.na.domain.data.product.dto.CisaAttributes;
import com.chubb.na.domain.data.product.dto.EnrichedFields;
import com.chubb.na.domain.data.product.dto.EventSchemaMessage;
import com.chubb.na.domain.data.product.dto.IngestionContextEvent;
import com.chubb.na.domain.data.product.dto.LMMConstruction;
import com.chubb.na.domain.data.product.dto.MultipartBody;
import com.chubb.na.domain.data.product.dto.PrivateEquity;
import com.chubb.na.domain.data.product.dto.SimMessage;
import com.chubb.na.domain.data.product.dto.SourceContextEvent;
import com.chubb.na.domain.data.product.dto.response.FinalResponse;
import com.chubb.na.domain.data.product.request.StateAbbreviationRequest;
import com.chubb.na.domain.data.product.response.CISAAssignmentResponse;
import com.chubb.na.domain.data.product.response.CISAExecutiveResponse;
import com.chubb.na.domain.data.product.response.InsuredMatchingResponse;
import com.chubb.na.domain.data.product.response.ProducerSearchResponse;
import com.chubb.na.domain.data.product.response.StateAbbreviationResponse;
import com.chubb.na.domain.data.product.restclient.CISAClient;
import com.chubb.na.domain.data.product.restclient.CibClient;
import com.chubb.na.domain.data.product.restclient.FinLinesClient;
import com.chubb.na.domain.data.product.restclient.RCClient;
import com.chubb.na.domain.data.product.restclient.UtilsClient;
import com.chubb.na.domain.data.product.restclient.impl.AdlsAPIAccessor;
import com.chubb.na.domain.drools.SICCodeCheck;
import com.chubb.na.domain.service.RuleApiService;
import com.chubb.na.domain.service.SicCodeService;
import com.chubb.na.domain.service.UserSearchService;
import com.chubb.na.domain.sim.SimService;
import com.chubb.na.domain.sim.beans.SimIncidentDetails;
import com.chubb.na.domain.token.util.TokenAuthUtil;
import com.chubb.na.domain.utils.Commons;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

@ApplicationScoped
public class Helper {

    @Inject
    Jsonb jsonb;

    @Inject
    EnrichmentHelper enrichmentHelper;

    @RestClient
    UtilsClient utilsClient;

    @RestClient
    RCClient rcClient;

    @RestClient
    FinLinesClient flClient;

    @RestClient
    CibClient cibClient;

    @Inject
    JsonHelper jsonHelper;

    @Inject
    AdlsAPIAccessor adlsAPIAccessor;

    @Inject
    CommonFieldsHelper commonFieldsHelper;

    @Inject
    RuleApiService ruleApiService;

    @Inject
    SicCodeService sicCodeService;

    @RestClient
    CISAClient cisaClient;

    @Inject
    UserSearchService userSearchService;

    @Inject
    TokenAuthUtil tokenAuthUtil;

    @Inject
    SimService simService;

    @ConfigProperty(name = "pe.email.identifier")
    String peEmailIdentifier;

    @ConfigProperty(name = "lmm.email.identifier")
    String lmmEmailIdentifier;

    @ConfigProperty(name = "uw.api.version")
    String apiVersion;
    @ConfigProperty(name = "uw.subscription.key")
    String subscriptionKey;
    @ConfigProperty(name = "uw.app.id")
    String appID;
    @ConfigProperty(name = "uw.app.key")
    String appKey;
    @ConfigProperty(name = "uw.resource.id")
    String resourceID;
    @ConfigProperty(name = "auth.version")
    String authVersion;

    @ConfigProperty(name = "broker.primary.contact")
    String brokerPrimaryContact;


    private static final Logger log = LoggerFactory.getLogger(Helper.class);


    /*
     * STAGE CODE FOR ENRICHMENT STARTED IS 1600
     * STATUS CODE FOR INPROGRESS IS 20
     */
    public JSONObject setInprogressStatus(JSONObject kafkaMessage) {
        JSONObject updatedMessage = kafkaMessage;
        log.info("-------STARTED UPDATING ENRICHMENT STATUS AS INPROGRESS-----");
        updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD, Commons.IN_PROGRESS_STATUS);
        updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STAGE_FIELD, Commons.ENRICHMENT_STARTED_STAGE);
        updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).getJSONArray("documents").getJSONObject(0).put(Commons.STAGE_FIELD, Commons.ENRICHMENT_STARTED_STAGE);
        log.info("-------FINISHED UPDATING ENRICHMENT STATUS AS INPROGRESS-----");
        return updatedMessage;
    }

    /*
     * CHECKING STAGE CODE
     */
    public String getStageCode(JSONObject kafkaMessage) {
        String stageCode = Commons.EMPTY_STRING;
        log.info("-------CHECKING STAGE CODE -----");
        stageCode = kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.STAGE_FIELD);
        log.info("-------CURRENT STAGE AT: " + stageCode +" ----------");
        return stageCode;
    }

    /*
     * CALL COMMON API TO GET DERIVED FIELDS
     */
    public String getCommonDerivedFieldsResponse(String inputJson) {
        String response = Commons.EMPTY_STRING;
        try {
            log.info("------CALLING COMMON API TO GET DERIVED FIELDS------ ");
            response = utilsClient.enrich(inputJson);
        } catch(Exception e) {
            log.warn("----------ERROR WHILE GETTING ENRICH DERIVED RESPONSE---------" + e.getMessage());
        }
        return response;
    }

    public String getBrokerAPIResponse(String request) {
        String response = request;
        try {
            log.info("----- CALLING UTILS CLIENT FOR BROKER MASHUP API -----");
            response = utilsClient.enrichBrokerFields(request);
        } catch (Exception e) {
            log.warn("----- ERROR WHILE GETTING THE BROKER FIELDS -----" + e.getMessage());
        }

        return response;
    }

    public JSONObject enrichAccountInfo(String enrichedString, JSONObject kafkaMessage) {
        log.info("----- ADDING ACCOUNT INFO TO SOURCE REFERENCE JSON -----");
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject enrichedJson = new JSONObject(enrichedString);
        String insuredName = jsonHelper.getInsuredName(enrichedJson);
        String sourceReference = kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD)
                .optString(Commons.SOURCE_REFERENCE_JSON);

        if(StringUtils.isBlank(sourceReference)) {
            JSONObject jsonObject = new JSONObject();
            sourceReference = jsonObject.toString();
        }

        try {
            JSONObject sourceReferenceJson = new JSONObject(sourceReference);

            AccountName accountName = new AccountName();
            accountName.setAccountName(insuredName);

            String accountInfoJson = objectMapper.writeValueAsString(accountName);
            sourceReferenceJson.put("accountInfo", new JSONObject(accountInfoJson));

            kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD)
                    .putOpt(Commons.SOURCE_REFERENCE_JSON, sourceReferenceJson.toString());
        } catch (Exception e) {
            log.warn("----- EXCEPTION WHILE UPDATING THE SOURCE REFERENCE JSON -----" + e);
        }

        return kafkaMessage;
    }

    /*
     * CALL COMMON API TO SUBMIT TO DOWNSTREAM SYSTEM
     */
    public String submitToDownstreamSystem(String standardizedJson, String lob, String queue, JSONObject kafkaMessage) {
        String response = Commons.EMPTY_STRING;
        String inputJson = updateInputJson(kafkaMessage, standardizedJson, queue);
        try {
            log.info("----------SUBMITTING TO DOWNSTREAM API FOR LOB : " + lob + " ---------");
            if (lob.equalsIgnoreCase(Commons.RC_LOB)) {
                response = utilsClient.submitRCToClearance(inputJson);
            } else if (lob.equalsIgnoreCase(Commons.CIB_LOB)) {
                response = utilsClient.submitCibToClearance(inputJson);
            } else if (lob.equalsIgnoreCase(Commons.FL_LOB)){
                try {
                    FinalResponse<JsonNode> resp = flClient.clearanceAndSubmission(inputJson, queue);
                    response = resp.isSuccess() ? Commons.SUBMITTED_TO_DOWNSTREAM_SYSTEM : Commons.EMPTY_STRING;
                } catch (Exception e){
                    log.error("------------------FAILED TO SUBMIT TO DOWNSTREAM SYSTEM --------------------" + e);
                }
            }
        } catch(Exception e) {
            log.warn("----------ERROR WHILE GETTING ENRICH DERIVED RESPONSE---------" + e.getMessage());
        }
        return response;
    }

    private String updateInputJson(JSONObject kafkaMessage, String standardizedJson, String queue) {
        JSONObject standardizedJsonObj = new JSONObject(standardizedJson);
        //Update Document Link
        String documentLink = jsonHelper.getDocumentLinkFromKafkaMessage(kafkaMessage);
        standardizedJsonObj = jsonHelper.updateDocumentLinkInInput(standardizedJsonObj, documentLink);
        standardizedJsonObj = jsonHelper.updateQueueNameInInput(standardizedJsonObj, queue);

        boolean isBrokerAPISubmission = checkForBrokerAPISubmission(kafkaMessage);
        standardizedJsonObj = jsonHelper.updateBrokerAPIFlag(standardizedJsonObj, isBrokerAPISubmission);

        standardizedJsonObj = jsonHelper.updateSTPFlag(kafkaMessage, standardizedJsonObj);
        //String sicCheck = ruleService.getSICCheckResult(jsonHelper.getSicCode(standardizedJsonObj));
        SICCodeCheck sic = new SICCodeCheck();
        sic.setSicCode(jsonHelper.getSicCode(standardizedJsonObj));
        String sicCheck = commonFieldsHelper.executeSICRules(sic, Commons.SIC_CHECK_DRL_FILENAME);
        standardizedJsonObj = jsonHelper.updateSicValidationResult(standardizedJsonObj, sicCheck);
        return standardizedJsonObj.toString();
    }

    /*
     * GET INSURED MATCHING AND PRODUCER MATCHING RESPONSE
     */
    public EnrichedFields getEnrichmentInfo(String inputJson) {
        EnrichedFields enrichedFields = new EnrichedFields();
        enrichedFields.setInsuredMatchingResponse(getInsuredMatchingResponse(inputJson));
        enrichedFields.setProducerSearchResponse(getProducerMatchingResponse(inputJson));
        return enrichedFields;
    }

    /*
     * CALLING ENRICHMENT API TO GET INSURED MATCHING RESPONSE
     */
    private InsuredMatchingResponse getInsuredMatchingResponse(String inputJson) {
        InsuredMatchingResponse insuredMatchingResponse = new InsuredMatchingResponse();
        try {
            insuredMatchingResponse = enrichmentHelper.getEnrichedInsured(new JSONObject(inputJson));
            log.info("---------INSURED MATCHING RESPONSE-----------------" + insuredMatchingResponse);
        } catch (Exception e) {
            log.warn("----------ERROR WHILE GETTING INSURED RESPONSE---------" + e.getMessage());
        }
        return insuredMatchingResponse;
    }

    /*
     * CALLING ENRICHMENT API TO GET PRODUCER MATCHING RESPONSE
     */
    private ProducerSearchResponse getProducerMatchingResponse(String inputJson) {
        ProducerSearchResponse producerSearchResponse = new ProducerSearchResponse();
        try {
            producerSearchResponse = enrichmentHelper.getEnrichedProducer(new JSONObject(inputJson));
            log.info("---------PRODUCER MATCHING RESPONSE-----------------" + producerSearchResponse);
        } catch(Exception e) {
            log.warn("----------ERROR WHILE GETTING PRODUCER MATCHING RESPONSE---------" + e.getMessage());
        }
        return producerSearchResponse;
    }

    /*
     * CALLING DERIVATION API'S BASED ON LOB
     */
    public String getDerivationResponse(String inputJson, String lob) {
        log.info("-----INSIDE CALLING DERIVATION API BASED ON LOB----");
        String response = Commons.EMPTY_STRING;
        try {
            if (lob.equalsIgnoreCase(Commons.RC_LOB)) {
                log.info("-------CALLING RC------");
                response = getRCResponse(inputJson);
                log.info("---------RC RESPONSE----------" + response);
            }else if (lob.equalsIgnoreCase(Commons.FL_LOB)) {
                log.info("-------CALLING FL------");
                response = getFLResponse(inputJson);
                log.info("---------FL RESPONSE----------" + response);
            } else if (lob.equalsIgnoreCase(Commons.CIB_LOB)) {
                log.info("----- CALLING CIB -----");
                response = getCibResponse(inputJson);
                log.info("---------CIB RESPONSE----------" + response);
            }
        } catch(Exception e) {
            log.warn("---------EXCEPTION WHILE GETTING "+lob+" DERIVED FIELDS RESPONSE----------" + response + " ERROR : " + e.getMessage());
        }
        return response;
    }

    public String enrichCommonFields(String standardizedInput, String submissionNumber) {
        JSONObject dataProductObject = new JSONObject(standardizedInput);
        try {

            dataProductObject = commonFieldsHelper.enrichTivAndLocationCount(dataProductObject);
            dataProductObject = commonFieldsHelper.enrichPayroll(dataProductObject);
            dataProductObject = sicCodeService.enrichSicCode(dataProductObject);

            /*
             * //D8INGEST-7542 - Individual License dataProductObject =
             * commonFieldsHelper.enrichInvidualLicensingProducerNumber(dataProductObject,
             * submissionNumber);
             */		} catch (JSONException e) {
            log.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE ENRICHING COMMON FIELDS : {} -----", submissionNumber, e);
        } catch (Exception e) {
            log.warn("----- SUBMISSION: {} :EXCEPTION WHILE ENRICHING COMMON FIELDS : {} -----", submissionNumber, e);
        }

        return dataProductObject.toString();
    }

    /*
     * CALLING RC DERIVATION API
     */
    private String getRCResponse(String inputJson)  {
        return rcClient.enrich(inputJson);
    }

    /*
     * CALLING FL DERIVATION API
     */
    private String getFLResponse(String inputJson)  {
        return flClient.enrich(inputJson);
    }

    /*
     * CALLING CIB DERIVATION API
     */
    private String getCibResponse(String inputJson)  {
        return cibClient.enrich(inputJson);
    }

    /*
     * UPLOAD FINAL JSON TO ADLS
     */
    public JSONObject uploadJsonToADLS(String inputJson, JSONObject kafkaMessage, int index) {
        JSONObject updatedMessage = kafkaMessage;
        try {
            String uploadedUri = upload(inputJson, kafkaMessage);
            log.info("------------UPLOADED URI------------------" + uploadedUri);
            //Update status in kafka message as completed
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STAGE_FIELD,
                    Commons.ENRICHMENT_COMPLETE_STAGE);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD,
                    Commons.COMPLETE_STATUS);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).getJSONArray(Commons.DOCUMENTS_FIELD)
                    .getJSONObject(index).put(Commons.URI_FIELD, uploadedUri);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).getJSONArray(Commons.DOCUMENTS_FIELD)
                    .getJSONObject(index).put(Commons.STAGE_FIELD, Commons.ENRICHMENT_COMPLETE_STAGE);
        } catch (Exception e) {
            log.error("----- ERROR WHILE UPLOADING FILE TO ADLS -----");
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.EVENT_TYPE_FIELD,
                    Commons.TECH_EXCEPTION_FIELD);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD, Commons.FAILED_STATUS);
        }
        return updatedMessage;
    }

    /*
     * CALLING ADLS UPLOAD API
     */
    private String upload(String inputJson, JSONObject kafkaMessage) {
        log.info("--------UPLOADING JSON---------");
        MultipartBody body = new MultipartBody();
        EventSchemaMessage eventSchemaMsg = buildEventSchemaMessage(kafkaMessage);
        String eventSchemaMsgJsonString = jsonb.toJson(eventSchemaMsg).toString();
        body.files = inputJson.getBytes();
        body.eventSchema = eventSchemaMsgJsonString;
        String response = adlsAPIAccessor.uploadFileToAdls(body);
        log.info("-----FILE HAS BEEN UPLOADED TO ADLS----------"+ response);
        return getUploadedURI(response);
    }

    private String getUploadedURI(String response) {
        JSONObject responseObj = new JSONObject(response);
        JSONArray documents = jsonHelper.getDocumentsInfoFromJson(responseObj);
        for (int i = 0; i < documents.length(); i++) {
            String stage = documents.getJSONObject(i).optString(Commons.STAGE_FIELD);
            if (stage.equals(Commons.ENRICHMENT_COMPLETE_STAGE))  {
                return documents.getJSONObject(i).optString(Commons.URI_FIELD);
            }
        }
        return Commons.EMPTY_STRING;
    }
    private EventSchemaMessage buildEventSchemaMessage(JSONObject messageObj) {
        log.info("-------STARTED BUILDING EVENT SCHEMA FOR UPLOAD--------");
        EventSchemaMessage eventSchema = EventSchemaMessage.builder()
                .sourceContext(SourceContextEvent.builder()
                        .submissionNumber(jsonHelper.getSubmissionNumberFromJson(messageObj))
                        .sourceSystemID(jsonHelper.getSourceSystemIdFromJson(messageObj))
                        .sourceReferenceNumber(jsonHelper.getSourceReferenceNumFromJson(messageObj))
                        .priority(jsonHelper.getPriorityFromJson(messageObj))
                        .channel(jsonHelper.getChannelFromJson(messageObj))
                        .sourceReferenceJson(jsonHelper.getSourceReferenceJson(messageObj))
                        .build())
                .ingestionContext(IngestionContextEvent.builder()
                        .useCaseId(jsonHelper.getUseCaseIdFromJson(messageObj))
                        .status(jsonHelper.getStatusFromJson(messageObj))
                        .stage(Commons.ENRICHMENT_COMPLETE_STAGE)
                        .region(jsonHelper.getRegionFromJson(messageObj))
                        .country(jsonHelper.getCountryFromJson(messageObj))
                        .lob(jsonHelper.getLobFromJson(messageObj))
                        .product(jsonHelper.getProductFromJson(messageObj))
                        .message(jsonHelper.getMessageFromJson(messageObj))
                        .layer(Commons.CURATED)
                        .numberOfSubmittedDocuments(jsonHelper.getNoOfSubmittedDocsFromJson(messageObj))
                        .build())
                .build();
        log.info("-------FINISHED BUILDING EVENT SCHEMA FOR UPLOAD--------");
        return eventSchema;
    }

    /*
     * SEND ERROR MESSAGE IN CASE OF DOWNSTREAM SUBMISSION FAILURE
     */
    public JSONObject sendErrorMessageToOrchestration(JSONObject kafkaMessage, int index) {
        JSONObject updatedMessage = kafkaMessage;
        try {
            log.info("----- UPDATING KAFKA MESSAGE FOR DOWNSTREAM SYSTEM ERROR -----");
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STAGE_FIELD, Commons.SUBMISSION_TO_DOWNSTREAM_FAILED);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.MESSAGE_FIELD, Commons.FAILED_TO_SUBMIT_TO_DOWNSTREAM_SYSTEM);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD, Commons.FAILED_STATUS);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).getJSONArray(Commons.DOCUMENTS_FIELD).getJSONObject(index).put(Commons.STAGE_FIELD, Commons.SUBMISSION_TO_DOWNSTREAM_FAILED);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.EVENT_TYPE_FIELD,Commons.TECH_EXCEPTION_FIELD);
        } catch (Exception e) {
            log.warn("----- ERROR WHILE UPDATING KAFKA MESSAGE -----"+e.getMessage());
        }
        return updatedMessage;
    }

    /*
     * SEND SUCCESS MESSAGE IN CASE OF DOWNSTREAM SUBMISSION SUCCESS
     */
    public JSONObject sendSuccessMessageToOrchestration(JSONObject kafkaMessage, int index) {
        JSONObject updatedMessage = kafkaMessage;
        try {
            log.info("----- UPDATING KAFKA MESSAGE FOR DOWNSTREAM SYSTEM SUCCESS -----");
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STAGE_FIELD, Commons.SUBMISSION_TO_DOWNSTREAM_COMPLETED);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.MESSAGE_FIELD, Commons.SUCCESFULLY_SUBMITTED_TO_DOWNSTREAM_SYSTEM);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD, Commons.COMPLETE_STATUS);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).getJSONArray(Commons.DOCUMENTS_FIELD).getJSONObject(index).put(Commons.STAGE_FIELD, Commons.SUBMISSION_TO_DOWNSTREAM_COMPLETED);
        } catch (Exception e) {
            log.warn("----- ERROR WHILE UPDATING KAFKA MESSAGE -----"+e.getMessage());
        }
        return updatedMessage;
    }

    /*
     * DO CLEARANCE CHECK
     */
    public String performClearanceCheck(String inputJson, String lob) {
        log.info("----- PERFORMING CLEARANCE CHECK -----");
        String coverageStatusJson = Commons.EMPTY_STRING;
        try {
            String clearanceResponse = Commons.EMPTY_STRING;
            if (lob.equalsIgnoreCase(Commons.RC_LOB)){
                clearanceResponse = rcClient.performClearanceCheck(inputJson);
            } else if (lob.equalsIgnoreCase(Commons.FL_LOB)){
                clearanceResponse = flClient.performClearanceCheck(inputJson);
            } else if (lob.equalsIgnoreCase(Commons.CIB_LOB)){
                clearanceResponse = cibClient.performClearanceCheck(inputJson);
            }
            if (StringUtils.isBlank(clearanceResponse)){
                log.info("------------FAILED TO GET RESPONSE FROM CLEARANCE CHECK, MOVING FORWARD WITH SDW AND LOGGING TECHNICAL EXCEPTION------------------");
                coverageStatusJson = Commons.TECH_EXCEPTION_FIELD;
            } else{
                coverageStatusJson = jsonHelper.getCoverageStatusData(clearanceResponse);
            }

        } catch (Exception e) {
            log.error("----- ERROR WHILE PERFORMING CLEARANCE CHECK LOGGING TECHNICAL ERROR-----" + e.getMessage());
            return Commons.TECH_EXCEPTION_FIELD;
        }
        return coverageStatusJson;
    }


    public JSONObject sendTechnicalErrorMessageToOrchestration(JSONObject kafkaMessage) {
        JSONObject updatedMessage = kafkaMessage;
        try {
            log.info("----- UPDATING KAFKA MESSAGE FOR DOWNSTREAM TECHNICAL ERROR -----");
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.EVENT_TYPE_FIELD,Commons.TECH_EXCEPTION_FIELD);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD, Commons.FAILED_STATUS);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.MESSAGE_FIELD, Commons.FAILED_TO_CONNECT_FOR_CLEARANCE_CHECK);
        } catch (Exception e) {
            log.warn("----- ERROR WHILE UPDATING KAFKA MESSAGE -----" + e.getMessage());
        }
        return updatedMessage;
    }

    public JSONObject sendClearanceBusinessErrorMessageToOrchestration(JSONObject kafkaMessage, String coverageStatus) {
        JSONObject updatedMessage = kafkaMessage;
        try {
            log.info("----- UPDATING KAFKA MESSAGE FOR CLEARANCE BUSINESS ERROR AS NO CLEARED COVERAGES FOUND-----");
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STAGE_FIELD, Commons.CLEARANCE_CHECK_FAILED);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.EVENT_TYPE_FIELD,Commons.BUSINESS_EXCEPTION_FIELD);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.STATUS_FIELD, Commons.FAILED_STATUS);
            updatedMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).put(Commons.MESSAGE_FIELD, Commons.CLEARANCE_CHECK_FAILED_MESSAGE + coverageStatus);
        } catch (Exception e) {
            log.warn("----- ERROR WHILE UPDATING KAFKA MESSAGE -----" + e.getMessage());
        }
        return updatedMessage;
    }

    public String getQueueName(JSONObject kafkaMessage){
        String queue = Commons.EMPTY_STRING;
        try{
            log.info("----- GETTING QUEUE NAME FROM KAFKA MESSAGE-----");
            queue = kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString(Commons.QUEUE_NAME_FIELD);
            log.info("----- QUEUE NAME : " + queue);
        } catch (Exception e) {
            log.warn("----- ERROR WHILE FETCHING DATA KAFKA MESSAGE; QUEUE NAME EMPTY-----" + e.getMessage());
        }
        return queue;
    }

    // D8INGEST-3726
    public boolean checkForBrokerAPISubmission(JSONObject kafkaMessage){
        boolean isBrokerAPISubmission = false;
        try{
            String sourceReference = kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD)
                    .optString(Commons.SOURCE_REFERENCE_JSON);

            if(StringUtils.isNotBlank(sourceReference)) {
                JSONObject sourceReferenceJson = new JSONObject(sourceReference);
                String primaryContact = sourceReferenceJson.optString("primary_contact");

                if (StringUtils.isNotBlank(primaryContact) && primaryContact.equalsIgnoreCase(brokerPrimaryContact)) {
                    isBrokerAPISubmission = true;
                    log.info("----- THE SUBMISSION BELONGS TO BROKER API -----");
                }
            }
        } catch (Exception e) {
            log.warn("----- ERROR WHILE CHECKING IF THE SUBMISSION BELONGS TO BROKER API -----" + e.getMessage());
        }
        return isBrokerAPISubmission;
    }

    public String preparePendingMessage(JSONObject kafkaMessage) {
        SimMessage simMsg = new SimMessage();
        simMsg.setSubmission_number(jsonHelper.getSubmissionNumberFromJson(kafkaMessage));
        simMsg.setSource_reference_number(jsonHelper.getSourceReferenceNumFromJson(kafkaMessage));
        simMsg.setQueue_name(getQueueName(kafkaMessage));
        simMsg.setEvent_type(Commons.SUBMISSION_COMPLETED);
        simMsg.setEvent_description(Commons.SUBMISSION_COMPLETED_DESC);
        return jsonb.toJson(simMsg);
    }

    public String validateProducerState(String standardizedString) {
        String updatedString = standardizedString;

        try {
            JSONObject standardizedJson = new JSONObject(standardizedString);
            String state = jsonHelper.getProducerState(standardizedJson);

            if(StringUtils.isNotBlank(state)) {
                StateAbbreviationRequest request = new StateAbbreviationRequest();
                request.setRequestedState(state);

                StateAbbreviationResponse response = ruleApiService.getStateAbbreviationService(request);

                if(StringUtils.isNotBlank(response.getMappedAbbreviation()) &&
                        !state.equalsIgnoreCase(response.getMappedAbbreviation())) {
                    standardizedJson = jsonHelper.updateProducerState(standardizedJson, response.getMappedAbbreviation());
                    updatedString = standardizedJson.toString();
                }
            }
        } catch (Exception e) {
            log.error("----- ERROR WHILE UPDATING THE PRODUCER STATE: " + e);
        }

        return updatedString;
    }

    public boolean checkForUserReviewNeeded(final String standardizedJson) {
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(new JSONObject(standardizedJson));
        boolean isUserReviewNeeded = false;
        boolean isCISA = jsonHelper.getCISAIdentifier(standardizedJson).equalsIgnoreCase("Y");
        boolean isCisaExecutiveNotDerived = checkCisaExecutiveNotDerived(standardizedJson);
        boolean isMajorAccount = jsonHelper.kickoutMajorAccountSubmissions(standardizedJson);
        boolean isAgencyLicenseNo = jsonHelper.getAgencyLicensing(new JSONObject(standardizedJson)).equalsIgnoreCase("N");
        isUserReviewNeeded = isMajorAccount || isAgencyLicenseNo || (isCISA && isCisaExecutiveNotDerived);
        log.info("------SUBMISSION: {} :USER REVIEW NEEDED ? : {} ------", submissionNumber, isUserReviewNeeded);
        return isUserReviewNeeded;
    }

    public boolean checkCisaExecutiveNotDerived(final String finalJson){
        return StringUtils.isBlank(jsonHelper.getCisaExecutiveEmail(finalJson));
    }

    public String deriveCISAFields(String finalJson) {
        JSONObject updatedObj = new JSONObject(finalJson);
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(updatedObj);
        try {
            Gson gson = new Gson();
            String prodBranch = jsonHelper.getProducerBranch(updatedObj);
            String agencyName = jsonHelper.getAgencyName(updatedObj);
            JSONArray policies = jsonHelper.getAllPolicies(updatedObj);

            log.info("----------------DERIVING CISA ATTRIBUTES FOR SUBMISSION: {} PROD_BRANCH: {} -----------------", submissionNumber, prodBranch);

            CisaAttributes cisaAttributes = new CisaAttributes();
            cisaAttributes.setCisaSubmission();

            CISAExecutiveResponse cisaExecutiveResponse = cisaClient.getCISAExecutive(getAuthToken(), apiVersion, subscriptionKey, prodBranch);
            // TODO: move the edge cases for email fetching to email response method
            if (null != cisaExecutiveResponse) {
                cisaAttributes.setCisaExecutiveName(cisaExecutiveResponse.getCisaExecutiveFirstName(), cisaExecutiveResponse.getCisaExecutiveLastName());
                if(Commons.CISA_EXECUTIVE_NAME_KIMBERLEY.equalsIgnoreCase(cisaExecutiveResponse.getCisaExecutiveFirstName() ) && Commons.CISA_EXECUTIVE_NAME_BARNES.equalsIgnoreCase(cisaExecutiveResponse.getCisaExecutiveLastName())) {
                    cisaAttributes.setCisaExecutiveEmail(userSearchService.getEmailFromMSExchange(Commons.CISA_EXECUTIVE_NAME_BARNES+","+Commons.CISA_EXECUTIVE_NAME_KIM, Commons.CISA_EXECUTIVE_DESIGNATION));
                }else {
                    cisaAttributes.setCisaExecutiveEmail(userSearchService.getEmailFromMSExchange(cisaExecutiveResponse.getCisaExecutiveName(), Commons.CISA_EXECUTIVE_DESIGNATION));
                }
                log.info("----------------RECEIVED CISA EXEC API RESPONSE: {}-----------------", cisaExecutiveResponse);
                updatedObj.getJSONObject(Commons.INGESTION_DATA).put(Commons.CISA, new JSONObject(gson.toJson(cisaAttributes)));
            } else {
                log.warn("---------------- CISA EXEC API FOR: {} RETURNED NULL-----------------", prodBranch);
            }

            for (int i = 0; i < policies.length(); i++) {
                JSONObject policy = policies.getJSONObject(i);
                String customerGroup = policy.optString(Commons.BUSINESS_SEGMENT_CODE);
                String policyType = policy.optString(Commons.LINE_OF_BUSINESS_CODE);
                log.info("----------------CALLING CISA API FOR: {}-{}-----------------", customerGroup, policyType);
                CISAAssignmentResponse cisaAssignmentResponse = cisaClient.getCISA(getAuthToken(), apiVersion, subscriptionKey,
                        prodBranch, customerGroup, agencyName, policyType);
                if (null != cisaAssignmentResponse) {
                    log.info("----------------RECEIVED CISA API RESPONSE: {}-----------------", cisaAssignmentResponse);
//                    String uwEmail = userSearchService.getEmailFromMSExchange(cisaAssignmentResponse.getUwName(), Commons.UNDERWRITER_DESIGNATION);
                    String uwEmail = "";
                    if(cisaAssignmentResponse.getUwName().equals(Commons.BOBBY_LACKER_EMAIL)) {
                        uwEmail = userSearchService.getEmailFromMSExchange(Commons.ROBERT_LACKER_EMAIL, Commons.UNDERWRITER_DESIGNATION);
                    }else {
                        uwEmail = userSearchService.getEmailFromMSExchange(cisaAssignmentResponse.getUwName(), Commons.UNDERWRITER_DESIGNATION);
                    }
                    String uaEmail = userSearchService.getEmailFromMSExchange(cisaAssignmentResponse.getUaName(), Commons.UNDERWRITER_DESIGNATION);

                    // Update UW UA ServiceBranch
                    policy = jsonHelper.updateUWUA(policy, Commons.UNDERWRITER, Commons.GIVEN_NAME, cisaAssignmentResponse.getUwFirstName());
                    policy = jsonHelper.updateUWUA(policy, Commons.UNDERWRITER, Commons.SURNAME, cisaAssignmentResponse.getUwLastName());
                    policy = jsonHelper.updateUWUA(policy, Commons.UNDERWRITER_ASSISTANT, Commons.GIVEN_NAME, cisaAssignmentResponse.getUaFirstName());
                    policy = jsonHelper.updateUWUA(policy, Commons.UNDERWRITER_ASSISTANT, Commons.SURNAME, cisaAssignmentResponse.getUaLastName());

                    policy.getJSONObject(Commons.UNDERWRITER).getJSONObject(Commons.COMMUNICATION).put(Commons.EMAIL_ADDRESS, uwEmail);
                    policy.getJSONObject(Commons.UNDERWRITER_ASSISTANT).getJSONObject(Commons.COMMUNICATION).put(Commons.EMAIL_ADDRESS, uaEmail);
                    policy.getJSONObject(Commons.UNDERWRITER).getJSONObject(Commons.ADDRESS).put(Commons.REGION_CODE, cisaAssignmentResponse.getServiceBranch());

                    policies.put(i, policy);
                } else {
                    log.warn("---------------- CISA API FOR: {}-{} RETURNED NULL-----------------", customerGroup, policyType);
                }
            }
            updatedObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.SUBMISSION).put(Commons.POLICY, policies);

        } catch (Exception e) {
            log.error("----- ERROR WHILE UPDATING THE CISA FIELDS FOR SUBMISSION NO. " + submissionNumber + " : " + e);
        }
        return updatedObj.toString();
    }

    private String getAuthToken() {
        String token = tokenAuthUtil.getBearerToken(appID, appKey, resourceID, authVersion).getAccessToken();
        return "Bearer " + token;
    }

    public String checkIdentifiers(Long incidentId, String submissionNumber) {
        String identifier = "";
        try {
            log.info("-----Calling SIM API to check Private Enquity Identifier ------ :" + submissionNumber);
            SimIncidentDetails incidentDetails = simService.getIncidentDetails(incidentId);
            log.info("---------Chubb Mail Box--------:"+incidentDetails.getCustomFields().getC().getChubb_mailbox().toUpperCase());
            identifier = incidentDetails.getCustomFields().getC().getChubb_mailbox().toUpperCase()
                    .contains(peEmailIdentifier.toUpperCase()) ? Commons.PE_IDENTIFIER : "";
            if(identifier.isEmpty()) {
                identifier = incidentDetails.getCustomFields().getC().getChubb_mailbox().toUpperCase()
                        .contains(lmmEmailIdentifier.toUpperCase()) ? Commons.LMM_IDENTIFIER : "";
            }
            log.info("-----------Identifier----------:"+identifier);
        } catch (Exception e) {
            log.error("----- ERROR WHILE Fetching Identifier from SIM Service API FOR SUBMISSION NO. "
                    + submissionNumber + " : " + e);
        }
        return identifier;
    }

    public String updatePEIdentifier(String standardizedJson, String peIdentifier) {

        JSONObject updatedObj = new JSONObject(standardizedJson);
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(updatedObj);
        log.info("-------- Updating PE Identifier for Submission --------- : "+submissionNumber);
        Gson gson = new Gson();
        PrivateEquity privateEquity = new PrivateEquity();
        privateEquity.setPeIdentifier(peIdentifier);
        updatedObj.getJSONObject(Commons.INGESTION_DATA).put(Commons.PRIVATE_EQUITY, new JSONObject(gson.toJson(privateEquity)));
        return updatedObj.toString();

    }

    public String updateLMMIdentifier(String standardizedJson, String lmmIdentifier) {

        JSONObject updatedObj = new JSONObject(standardizedJson);
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(updatedObj);
        log.info("-------- Updating LMM Identifier for Submission --------- : "+submissionNumber);
        Gson gson = new Gson();
        LMMConstruction lmmConstruction = new LMMConstruction();
        lmmConstruction.setLmmIdentifier(lmmIdentifier);
        updatedObj.getJSONObject(Commons.INGESTION_DATA).put(Commons.LMM_LOB, new JSONObject(gson.toJson(lmmConstruction)));
        return updatedObj.toString();

    }

}

