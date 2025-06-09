package org.example.helper;

package com.chubb.na.domain.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chubb.na.domain.data.product.dto.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chubb.na.domain.data.product.request.ClimateRequest;
import com.chubb.na.domain.data.product.response.ClimateResponse;
import com.chubb.na.domain.service.RuleApiService;
import com.chubb.na.domain.sim.SimService;
import com.chubb.na.domain.sim.beans.SimIncidentDetails;
import com.chubb.na.domain.utils.Commons;
import com.chubb.na.domain.utils.FieldConstants;
import com.chubb.na.domain.utils.TimeUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.ProcessingException;

@ApplicationScoped
public class JsonHelper {

    private static final Logger logger = LoggerFactory.getLogger(JsonHelper.class);

    @Inject
    Jsonb jsonb;

    @Inject
    SimService simService;

    @Inject
    RuleApiService ruleService;

    private static final String LICENSING_COMPANY = "Federal Insurance Company";
    private static final String LICENSING_STATUS = "Open";
    private static final String DATE_FORMAT_YYYYMMDD= "MM/dd/uuuu";

    public static final long HOUR = 3600*1000;
    public String removeEnrichedDataFromJson(String jsonString) {
        try {
            JsonObject jsonObject = jsonb.fromJson(jsonString, JsonObject.class);
            JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder(jsonObject);
            //jsonObjBuilder.remove(Commons.ENRICHED_FIELD);
            jsonObjBuilder.remove(Commons.KAFKA_MESSAGE);
            JsonObject updatedJsonObject = jsonObjBuilder.build();
            jsonString = jsonb.toJson(updatedJsonObject);
        } catch (Exception e) {
            logger.error("ERROR WHILE REMOVING ERICHED DATA FIELD FORM FINAL JSON:- " + e.getMessage());
        }

        return jsonString;
    }

    public String removeCoverageStatusField(String jsonString) {
        String finalJson = Commons.EMPTY_STRING;

        try {
            JsonObject jsonObject = jsonb.fromJson(jsonString, JsonObject.class);
            JsonObjectBuilder jsonObjBuilder = Json.createObjectBuilder(jsonObject);
            jsonObjBuilder.remove(Commons.COVERAGE_CLEARANCE_STATUS);
            JsonObject updatedJsonObject = jsonObjBuilder.build();
            finalJson = jsonb.toJson(updatedJsonObject);
        } catch (Exception e) {
            logger.error("ERROR WHILE REMOVING COVERAGE STATUS FIELD FORM FINAL JSON:- " + e.getMessage());
        }

        return finalJson;
    }

    public String getCoverageStatusData(String jsonString) {
        String coverageStatusJson = Commons.EMPTY_STRING;

        try {
            JsonObject jsonObject = jsonb.fromJson(jsonString, JsonObject.class);
            if (jsonObject.containsKey(Commons.COVERAGE_CLEARANCE_STATUS)){
                JsonArray updatedJsonArray = jsonObject.getJsonArray(Commons.COVERAGE_CLEARANCE_STATUS);
                for (JsonValue value : updatedJsonArray) {
                    JsonObject obj = (JsonObject) value;
                    String coverage = obj.getString("policyType");
                    String status = obj.getString("status");
                    // Process the string values as needed
                    coverageStatusJson = coverageStatusJson + coverage + ":" + status + ";";
                }
                logger.info("COVERAGE STATUS INFO FROM :- " + coverageStatusJson);
            }
        } catch (Exception e) {
            logger.error("ERROR WHILE GETTING COVERAGE STATUS FROM JSON:- " + e.getMessage());
        }

        return coverageStatusJson;
    }

    public String getSimProduct(String inputJson) {
        JSONObject updatedJson = new JSONObject(inputJson);
        String simLob = updatedJson.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.SUBMISSION).optString(Commons.SIM_PRODUCT);
        return simLob;
    }

    public String getMarketSegment(String inputJson) {
        JSONObject updatedJson = new JSONObject(inputJson);
        String segment = Commons.EMPTY_STRING;
        boolean lmm = false;
        segment = updatedJson.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.SUBMISSION).optString(Commons.MIDDLE_MARKET);
        if (StringUtils.isNotBlank(segment)){
            JSONArray coverages = updatedJson.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.SUBMISSION).getJSONArray(Commons.POLICY);
            if (!coverages.isEmpty()) {
                for (int i=0; i<coverages.length(); i++) {
                    JSONObject coverage = coverages.getJSONObject(i);
                    if(coverage.isEmpty()) {
                        continue;
                    }
                    if (coverage.has("businessSegmentCode")){
                        if (StringUtils.isNotBlank(coverage.optString("businessSegmentCode"))){
                            if (coverage.optString("businessSegmentCode").endsWith("L")){
                                lmm = true;
                            }
                        }
                    }
                }
            }
            if (lmm){
                segment = "Lower Middle Market";
            } else {
                segment = "Middle Market";
            }
        }
        return segment;
    }

    public String getTransaction(String inputJson) {
        JSONObject updatedJson = new JSONObject(inputJson);
        String transaction = Commons.EMPTY_STRING;
        transaction = Commons.NEWLINE_TRANSACTION;
        return transaction;
    }

    public String updateEnrichmentFieldsInIngestionData(String finalJson, String enrichedString, String lob, JSONObject message, EnrichedFields enrichedFields) {

        JSONObject obj = new JSONObject(finalJson);
        JSONObject enrichedJson = new JSONObject(enrichedString);
        logger.info("----------- ENRICHED STRING ------" + enrichedString);
        String ownership = getOwnershipField(enrichedJson);
        String insuredFEIN = getInsuredFeinField(enrichedJson);
        String agencyMDMID = getAgencyMDMIDField(enrichedJson);
        String agencyLicensing = getAgencyLicensingField(enrichedJson,obj);
        String websiteURL = getWebsiteUrlField(enrichedJson);
        String cuw = getCuwField(obj, enrichedJson);
        String revenue = getRevenueField(obj, enrichedJson);
        String nacis = getNAICSField(enrichedJson);
        String producerCodeACE = getACEProducerCode(obj, enrichedJson);
        String producerCodeChubb = getChubbProducerCode(obj,enrichedJson);
        String agencyName = getChubbAgencyName(obj, enrichedJson);
        obj = updateOwnership(obj, ownership);
        obj = updateInsurredFEIN(obj, insuredFEIN);
        obj = updateAgencyMDMID(obj, agencyMDMID);
        obj = updateAgencyLicensing(obj, agencyLicensing);
        obj = updateWebsiteURL(obj, websiteURL);
        obj = updateCuw(obj, cuw);
        obj = updateRevenue(obj, revenue);
        obj = updateInsuredAddress(obj, enrichedJson);
        obj = updateNAICS(obj, nacis);
        obj = enrichReceivedDate(obj);
        obj = enrichCentralizedMailboxDate(obj, getIncidentFromKafkaMessage(message));
        obj = enrichUnderwriterReceivedDate(obj);
        obj = updateSimReferenceNumber(obj, getIncidentRefNoFromKafkaMessage(message));
        obj = updateACEProducerCode(obj, producerCodeACE);
        obj = updateChubbProducerCode(obj, producerCodeChubb);
        obj = updateAgencyName(obj, agencyName);
        obj = updateBrokerFieldsInIngestionData(obj);
        obj = enrichEffectiveAndEnrichmentDates(obj);
        if (Commons.RC_LOB.equalsIgnoreCase(lob)) {
            String businessSegment = getBusinessSegmentField(enrichedJson);
            obj = updateBusinessSegment(obj, businessSegment);
        }
        if (Commons.FL_LOB.equalsIgnoreCase(lob)) {
            String prodSubcode = getProdSubcodeField(enrichedJson);
            obj = updateProdSubcode(obj, prodSubcode);
        }
        obj = enrichIncumbentCarrier(obj);
        obj = updateAgencyAddress(obj, enrichedJson);
        if (Commons.RC_LOB.equalsIgnoreCase(lob)) {
            String climateTechIndicator = getClimateTechIndicator(obj, enrichedJson);
            obj = updateClimateInfo(obj, enrichedFields, climateTechIndicator);
        }

        return obj.toString();
    }

    public JSONObject updateClimateInfo(JSONObject dataProductObject, EnrichedFields enrichedFields, String indicator) {
        JSONObject updatedJson = dataProductObject;
        try {
            logger.info("----- UPDATING CLIMATE INFO-----");
            BusinessDetails businessDetails = enrichedFields.getInsuredMatchingResponse().getOutput().get(0).getBusinessDetails();
            ClimateTech climateTech = new ClimateTech();
            String srcCod = businessDetails.getClimateAsset()!=null ?businessDetails.getClimateAsset().getSrcCd(): Commons.EMPTY_STRING;
            //D8INGEST-6905
            climateTech.setIndicator(indicator);
            climateTech.setClimateSector(defaultClimateSector(indicator));
            climateTech.setClimateAsset(lookupClimateTechDetails(businessDetails.getClimateAsset(), Commons.CLIMATE_ASSET));
            climateTech.setClimateDataSource(lookupClimateDescription(srcCod, Commons.DEFAULT_LOOK_UP));
            updatedJson.getJSONObject("ingestionData").putOpt("climateTech", new JSONObject(climateTech));
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE UPDATING CLIMATE INFO -----", e);
        }
        return updatedJson;
    }

    protected ClimateTechDetails defaultClimateSector(String indicator) {
        ClimateTechDetails climateTechDetails = new ClimateTechDetails();

        if(StringUtils.isNotBlank(indicator) && indicator.equalsIgnoreCase(Commons.CLIMATE_TECH_INDICATOR_Y)) {
            logger.info("----- CLIMATE TECH INDICATOR IS YES. DEFAULTING CLIMATE SECTOR VALUE AND DESCRIPTION -----");

            climateTechDetails.setValue(Commons.CLIMATE_SECTOR_DEFAULT_VALUE);
            climateTechDetails.setDescription(Commons.CLIMATE_SECTOR_DEFAULT_DESCRIPTION);
        } else {
            climateTechDetails.setValue(Commons.EMPTY_STRING);
            climateTechDetails.setDescription(Commons.EMPTY_STRING);
        }

        return climateTechDetails;
    }

    private ClimateTechDetails lookupClimateTechDetails(final Climate climateInfo, final String lookup) {
        ClimateTechDetails climateTechDetails = new ClimateTechDetails();

        if(null!=climateInfo && StringUtils.isNotBlank(climateInfo.getValue())) {
            climateTechDetails.setValue(climateInfo.getValue());
            climateTechDetails.setDescription(lookupClimateDescription(climateInfo.getValue(), lookup));

            if(StringUtils.isBlank(climateTechDetails.getDescription())){
                climateTechDetails.setValue(Commons.EMPTY_STRING);
            }
        }else {
            climateTechDetails.setValue(Commons.EMPTY_STRING);
            climateTechDetails.setDescription(Commons.EMPTY_STRING);
        }



        return climateTechDetails;
    }

    private String lookupClimateDescription(String value, String lookup) {
        String climateDescription = Commons.EMPTY_STRING;

        if (StringUtils.isNotBlank(value)) {
            ClimateResponse response = callClimateRuleService(value, lookup);
            if (null != response && StringUtils.isNotBlank(response.getDescription())) {
                climateDescription = response.getDescription();
            }
        }
        return climateDescription;
    }



    public ClimateResponse callClimateRuleService(String srcCd, String lookup) {
        ClimateRequest request = new ClimateRequest();
        request.setCode(srcCd);
        request.setLookup(lookup);
        logger.info("------CLIMATE REQUEST------" + request);
        ClimateResponse response = ruleService.getClimateDescription(request);
        logger.info("------CLIMATE RESPONSE-----" + response);
        return response;
    }

    public String getSicCode(final JSONObject dataProductObject) {
        String sicCode = "";
        try {
            sicCode = dataProductObject.getJSONObject("ingestionData").getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0)
                    .optString("SIC");
        } catch (JSONException e) {
            logger.error("-----EXCEPTION WHILE GETTING THE SIC CODE-----" + e);
        }

        return sicCode;
    }

    public JSONObject getSicCodeObject(JSONObject dataProductObject) {
        JSONObject sicCodeObject = new JSONObject();

        try {
            JSONArray dataElementSource = dataProductObject.getJSONArray("dataElementSource");

            for (int i = 0; i < dataElementSource.length(); i++) {
                JSONObject element = dataElementSource.getJSONObject(i);
                if (Commons.DATA_ELEMENT_PRIMARY_SIC_CODE.equalsIgnoreCase(element.optString("dataElementName"))) {
                    sicCodeObject = element;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("----- ERROR WHILE GETTING THE SIC CODE OBJECT -----" + e);
        }

        return sicCodeObject;
    }

    public JSONObject updateSicValidationResult(final JSONObject dataProductObject, final String result) {
        JSONObject updatedJson = dataProductObject;
        try {
            logger.info("----- UPDATING SIC CODE VALIDATION RESULT: " + result + " -----");
            updatedJson.getJSONObject("ingestionData").getJSONObject("submission")
                    .putOpt("validSIC", result);
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE UPDATING SIC CODE VALIDATION RESULT -----", e);
        }
        return updatedJson;
    }

    private JSONObject updateAgencyAddress(JSONObject obj, JSONObject enrichedJson) {
        logger.info("-----UPDATING AGENCY ADDRESS-----");
        JSONObject updatedObj = obj;
        try {
            JSONArray producerEnrichmentOutputArr = enrichedJson.getJSONObject("producerSearchResponse")
                    .getJSONArray("output");
            if (null != producerEnrichmentOutputArr && producerEnrichmentOutputArr.length() > 0) {
                JSONArray addressArr = updatedObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.PRODUCER)
                        .getJSONArray("address");
                for (int i = 0; i < addressArr.length(); i++) {
                    if (addressArr.getJSONObject(i).optString("typeCode").equals(Commons.AGENCY)) {
                        String enrichedStreet1 = producerEnrichmentOutputArr.getJSONObject(0).optString("busnAddr1LineTxt");
                        String enrichedCity = producerEnrichmentOutputArr.getJSONObject(0).optString("busnCityName");
                        String enrichedState = producerEnrichmentOutputArr.getJSONObject(0).optString("busnStCode");
                        String enrichedZipcode = producerEnrichmentOutputArr.getJSONObject(0).optString("busnZipCode");
                        addressArr.getJSONObject(i)
                                .putOpt("line1", enrichedStreet1)
                                .putOpt("city", enrichedCity)
                                .putOpt("stateOrProvinceCode", enrichedState)
                                .putOpt("postalCode", enrichedZipcode);

                    }
                }
            }
        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE UPDATING AGENCY ADDRESS IN JSON---" + e);
        }
        return updatedObj;


    }

    private JSONObject enrichIncumbentCarrier(JSONObject obj) {
        logger.info("----- ENRICHING INCUMBENT CARRIER -----");
        JSONObject updatedObj = obj;

        try{
            JSONObject policyObj = updatedObj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0);
            String incumbentCarrier = policyObj.optString("carrierCode");

            if(StringUtils.isBlank(incumbentCarrier)) {
                logger.info("----- INCUMBENT CARRIER IS EMPTY. ENRICHING IT WITH THE VALUE UNKNOWN -----");
                policyObj.put("carrierCode", Commons.DEFAULT_INCUMBENT_CARRIER);
            }
        } catch(JSONException e) {
            logger.warn("----- EXCEPTION WHILE ENRICHING THE INCUMBENT CARRIER -----");
        }

        return updatedObj;
    }

    private JSONObject enrichEffectiveAndEnrichmentDates(JSONObject obj) {
        JSONObject updatedObj = obj;
        try {
            JSONObject policyObj = obj.getJSONObject("ingestionData").getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0);
            String receivedDate = obj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .optString("receivedDate");
            //D8INGEST-4946
            String effectiveDate = policyObj.optString("effectiveDate");
            if (StringUtils.isBlank(effectiveDate) && StringUtils.isNotBlank(receivedDate)) {
                logger.info("----- EFFECTIVE DATE IS BLANK, DERIVING FROM SUBMISSION RECEIVED DATE -----");
                effectiveDate = calculateEffectiveDate(receivedDate);
            } else {
                effectiveDate = parseDate(effectiveDate);
            }
            //D8INGEST-7606 - START
            boolean isValidEffectiveDate = validateEffectiveDate(obj, effectiveDate, receivedDate);
            if (!isValidEffectiveDate) {
                effectiveDate = null;
            }
            //D8INGEST-7606 - END
            // Update expiration date
            logger.info("----- ENRICHING EXPIRATION DATE -----");
            String expirationDate = policyObj.optString("expirationDate");
            if (StringUtils.isNotEmpty(effectiveDate)) {
                expirationDate = calculateExpirationDate(effectiveDate);
            }

            // Update Effective date and Expiry Date
            updatedObj.getJSONObject("ingestionData").getJSONObject("submission")
                    .getJSONArray("policy")
                    .getJSONObject(0)
                    .put("effectiveDate", effectiveDate)
                    .put("expirationDate", expirationDate);

        } catch (Exception e) {
            logger.warn("----- EXCEPTION WHILE UPDATING EXPIRATION/EFFECTIVE DATE -----" + e);
        }
        return updatedObj;

    }

    private Boolean validateEffectiveDate(final JSONObject obj, final String effectiveDate, final String receivedDate) {
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        logger.info("------ SUBMISSION: {} :STARTED VALIDATING EFFECTIVE DATE: {}", submissionNumber, effectiveDate);
        LocalDateTime parsedEffectiveDate = TimeUtil.parseDate(effectiveDate);
        LocalDateTime parsedReceivedDate = TimeUtil.parseDate(receivedDate);
        logger.info("------ SUBMISSION: {} : PARSED DATES: EFFECTIVE DATE: {} AND RECEIVED DATE: {}", submissionNumber, parsedEffectiveDate, parsedReceivedDate);
        if (parsedEffectiveDate.isBefore(parsedReceivedDate.minusDays(29)) ||
                parsedEffectiveDate.isAfter(parsedReceivedDate.plusDays(179))) {
            logger.info("------ SUBMISSION: {} :INVALID EFFECTIVE DATE", submissionNumber);
            return false;
        }
        logger.info("------ SUBMISSION: {} :VALID EFFECTIVE DATE", submissionNumber);
        return true;
    }

    private String calculateEffectiveDate(String receivedDate) {
        LocalDateTime enrichedEffectiveDate = TimeUtil.parseDate(receivedDate).plusDays(30);
        String effectiveDate = enrichedEffectiveDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
        return effectiveDate;
    }

    private String calculateExpirationDate(String effectiveDate) {
        LocalDateTime enrichedExpirationDate = TimeUtil.parseDate(effectiveDate).plusYears(1);
        String expirationDate = enrichedExpirationDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
        return expirationDate;
    }

    private String parseDate(String dateString) {
        LocalDateTime parsedDate = TimeUtil.parseDate(dateString);
        String finalDate = parsedDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD));
        return finalDate;
    }

    private JSONObject enrichReceivedDate(JSONObject jsonobj) {
        logger.info("----- ENRICHING RECEIVED DATE -----");
        JSONObject requestObject = jsonobj;
        try {
            String eventDate = requestObject.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy")
                    .getJSONObject(0)
                    .getJSONObject("underwriter")
                    .getJSONObject("event")
                    .optString("eventDate");
            String eventTime = requestObject.getJSONObject("ingestionData").getJSONObject("submission")
                    .optString("receivedDate");

            if (StringUtils.isNotBlank(eventTime)) {
                eventDate = eventDate.trim() + " " + eventTime.trim();
            }
            logger.info("----- EVENT DATE BEFORE CONVERSION: " + eventDate + " -----");
            if(StringUtils.isNotBlank(eventDate)) {
                LocalDateTime fomrattedDate =TimeUtil.parseDate(eventDate);
                String enrichedDate = TimeUtil.convertDateTo12HrFormat(fomrattedDate);

                logger.info("----- ENRICHED EVENT DATE: " + enrichedDate + " -----");

                requestObject.getJSONObject("ingestionData")
                        .getJSONObject("submission")
                        .getJSONArray("policy")
                        .getJSONObject(0)
                        .getJSONObject("underwriter")
                        .getJSONObject("event")
                        .put("eventDate", enrichedDate);
            }

        } catch (JSONException e) {
            logger.warn("----- EXCEPTION WHILE ENRICHING EVENT DATE -----"+e.getMessage());
        } catch (Exception ex) {
            logger.warn("----- EXCEPTION IN ENRICH RECEIVED DATE -----"+ex.getMessage());
        }

        return requestObject;
    }


    private JSONObject updateAgencyName(JSONObject jsonobj, String agencyName) {
        logger.info("-----UPDATING AGENCY NAME-----");
        JSONObject updatedObj = jsonobj;
        try {
            updatedObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.PRODUCER)
                    .put(Commons.FULL_NAME, agencyName);
        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE UPDATING AGENCY NAME IN JSON---" + e);
        }
        return updatedObj;
    }

    private String getChubbAgencyName(JSONObject jsonObj, JSONObject enrichedJson) {
        String agencyName = Commons.EMPTY_STRING;
        String enrichedAgencyName = getEnrichedChubbAgencyName(enrichedJson);
        String extractedAgencyName = getExtractedChubbAgencyName(jsonObj);
        if (StringUtils.isNotBlank(enrichedAgencyName)) {
            agencyName = enrichedAgencyName;
        } else {
            agencyName = extractedAgencyName;
        }
        return agencyName;
    }

    private String getEnrichedChubbAgencyName(JSONObject enrichedJson) {
        String agencyName = Commons.EMPTY_STRING;
        try {
            agencyName = enrichedJson.getJSONObject(Commons.PRODUCER_SEARCH_RESPONSE).getJSONArray(Commons.OUTPUT).getJSONObject(0)
                    .optString(Commons.PROD_CNTRCTD_NAME);
        } catch (JSONException e) {
            logger.error("--------------- ERROR WHILE GETTING AGENCY NAME --------" + e);
        } catch (Exception e) {
            logger.error("-------EXCEPTION WHILE GETTING ENRICHED PRODUCER NAME------" + e);
        }
        return agencyName;
    }

    private String getExtractedChubbAgencyName(JSONObject jsonObj) {
        String extractedAgencyName = Commons.EMPTY_STRING;
        try {
            extractedAgencyName = jsonObj.getJSONObject(Commons.INGESTION_DATA)
                    .getJSONObject(Commons.PRODUCER)
                    .optString(Commons.FULL_NAME);
        } catch (JSONException e) {
            logger.error("--------------- ERROR WHILE GETTING AGENCY NAME --------" + e);
        } catch (Exception e) {
            logger.error("--------------- EXCPETION WHILE GETTING EXTRACTED AGENCY NAME --------" + e);
        }
        return extractedAgencyName;
    }

    private JSONObject enrichCentralizedMailboxDate(JSONObject requestObject, Long incidentId) {
        logger.info("----- ENRICHING CENTRALIZED MAILBOX DATE -----");
        try {
            SimIncidentDetails simResponse = simService.getIncidentDetails(incidentId);
            if (simResponse != null) {
                String simReceivedDate = simResponse.getCreatedTime();
                String enrichedDate = simReceivedDate;
                if (StringUtils.isNotBlank(simReceivedDate)) {

                    LocalDateTime utcDateTime = LocalDateTime.parse(simReceivedDate, DateTimeFormatter.ISO_DATE_TIME);
                    ZoneId estZone = ZoneId.of("America/New_York");
                    ZonedDateTime estDateTime = ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))
                            .withZoneSameInstant(estZone);
                    enrichedDate = estDateTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a"));

                    logger.info("----- ENRICHED CENTRALIZED MAILBOX DATE: " + enrichedDate + " -----");

                    requestObject.getJSONObject("ingestionData").getJSONObject("submission").put("receivedDate",
                            enrichedDate);
                }
            }
        } catch (JSONException e) {
            logger.warn("----- EXCEPTION WHILE ENRICHING CENTRALIZED MAILBOX DATE -----" + e.getMessage());
        }  catch (ProcessingException e) {
            logger.warn("----- EXCEPTION WHILE CONNECTING TO SIM -----" + e.getMessage());
        } catch (Exception ex) {
            logger.warn("----- EXCEPTION IN ENRICH CENTRALIZE MAIL BOX DATE----- " + ex.getMessage());
        }

        return requestObject;
    }

    protected JSONObject enrichUnderwriterReceivedDate(JSONObject requestObject) {
        logger.info("----- VERIFYING UNDERWRITER RECEIVED DATE -----");

        try {
            JSONObject eventObject = requestObject.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("underwriter")
                    .getJSONObject("event");

            String uwReceivedDate = eventObject.optString("eventDate");

            if(StringUtils.isBlank(uwReceivedDate)) {
                String centralizedMailboxDate = requestObject.getJSONObject("ingestionData")
                        .getJSONObject("submission")
                        .optString("receivedDate");

                if(StringUtils.isNotBlank(centralizedMailboxDate)) {
                    logger.info("----- DEFAULTING THE UNDERWRITER RECEIVED DATE WITH CENTRALIZED MAILBOX DATE: " +
                            centralizedMailboxDate + " -----");

                    eventObject.putOpt("eventDate", centralizedMailboxDate);
                }
            }

        } catch(JSONException e) {
            logger.warn("----- EXCEPTION WHILE DEFAULTING UNDERWRITER RECEIVED DATE -----" + e.getMessage());
        }

        return requestObject;
    }

    public JSONObject updateSimReferenceNumber(JSONObject requestObject, String incidentRefNo){
        logger.info("----- ENRICHING SIM REFERENCE NUMBER -----" + incidentRefNo);
        try{
            requestObject.getJSONObject("ingestionData")
                    .getJSONObject("messageInformation").putOpt("sourceSystemReferenceNumber", incidentRefNo);
        } catch (JSONException e){
            logger.warn("----- EXCEPTION WHILE ENRICHING SIM REFERENCE NUMBER -----" + e.getMessage());
        }
        return requestObject;
    }

    public JSONObject updateBrokerFieldsInIngestionData(JSONObject jsonObj) {
        JSONObject obj = jsonObj;

        String brokerFirstName = getBrokerFirstNameFromJSON(jsonObj);
        String brokerLastName = getBrokerLastNameFromJSON(jsonObj);

        obj = updateBrokerFirstName(obj, brokerFirstName);
        obj = updateBrokerLastName(obj, brokerLastName);

        return jsonObj;

    }

    public String getBrokerFirstNameFromJSON(JSONObject jsonObj) {
        String brokerFirstName = Commons.EMPTY_STRING;
        try {
            brokerFirstName = jsonObj.getJSONObject("ingestionData").getJSONObject("producer").getJSONObject("contact")
                    .optString("givenName");
            if (StringUtils.isNotBlank(brokerFirstName)) {
                brokerFirstName = brokerFirstName.split(Commons.SPACE)[0];
            }
            return brokerFirstName;
        } catch (JSONException e) {
            logger.warn("-----EXCEPTION WHILE GETTING PRODUCER BRANCH FROM JSON-----" + e);
            return Commons.EMPTY_STRING;
        } catch (Exception e) {
            logger.warn("---EXCEPTION WHILE GETTING PRODUCER CODE FROM JSON---" + e);
            return Commons.EMPTY_STRING;
        }
    }

    public String getBrokerLastNameFromJSON(JSONObject jsonObj) {
        String brokerLastName = Commons.EMPTY_STRING;
        try {
            brokerLastName = jsonObj.getJSONObject("ingestionData").getJSONObject("producer").getJSONObject("contact")
                    .optString("surname");
            if (StringUtils.isNotBlank(brokerLastName)) {
                brokerLastName = brokerLastName
                        .split(Commons.SPACE)[brokerLastName.split(Commons.SPACE).length - 1];
            }
            return brokerLastName;

        } catch (JSONException e) {
            logger.warn("-----EXCEPTION WHILE GETTING PRODUCER BRANCH FROM JSON-----" + e);
            return Commons.EMPTY_STRING;
        } catch (Exception e) {
            logger.warn("---EXCEPTION WHILE GETTING PRODUCER CODE FROM JSON---" + e);
            return Commons.EMPTY_STRING;
        }
    }

    private JSONObject updateBrokerFirstName(JSONObject obj, String brokerFirstName) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("producer").getJSONObject("contact").put("givenName", brokerFirstName);
        } catch (JSONException e) {
            logger.warn("---EXCEPTION WHILE UPDATING BROKER FIRST NAME JSON---" + e);
        } catch (Exception e) {
            logger.warn("---EXCEPTION WHILE UPDATING BROKER FIRST NAME JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateBrokerLastName(JSONObject obj, String brokerLastName) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("producer").getJSONObject("contact").put("surname", brokerLastName);
        } catch (JSONException e) {
            logger.warn("---EXCEPTION WHILE UPDATING BROKER LAST NAME JSON---" + e);
        } catch (Exception e) {
            logger.warn("---EXCEPTION WHILE UPDATING BROKER LAST NAME JSON---" + e);
        }
        return updatedObj;
    }

    private String getACEProducerCode(JSONObject jsonObj, JSONObject enrichedJson) {
        String producerCodeACE = Commons.EMPTY_STRING;
        //String extractedProdACECode = getExtractedProducerACECode(jsonObj);
        try {
            //if (StringUtils.isNotBlank(extractedProdACECode)) {
            //	producerCodeACE = extractedProdACECode;
            //} else {
            producerCodeACE = enrichedJson.getJSONObject(Commons.PRODUCER_SEARCH_RESPONSE).getJSONArray(Commons.OUTPUT).getJSONObject(0)
                    .optString(Commons.PAS_PROD_CODE);
            //}
        } catch (JSONException e) {
            logger.error("--------------- ERROR WHILE GETTING ACE PRODUCER CODE --------" + e);
        } catch (Exception e) {
            logger.error("--------------- EXCEPTION WHILE GETTING ACE PRODUCER CODE --------" + e);
        }

        return producerCodeACE;
    }

    private String getChubbProducerCode(JSONObject jsonObj, JSONObject enrichedJson) {
        String producerCodeChubb = Commons.EMPTY_STRING;
        String extractedProdChubbCode = getExtractedProducerChubbCode(jsonObj);
        String cisaIdentifier = getCISAIdentifier(jsonObj.toString());
        try {
            if(cisaIdentifier.equalsIgnoreCase(Commons.Y)) { // D8INGEST-6837
                if (StringUtils.isNotBlank(extractedProdChubbCode)) {
                    producerCodeChubb = extractedProdChubbCode;
                } else {
                    producerCodeChubb = enrichedJson.getJSONObject(Commons.PRODUCER_SEARCH_RESPONSE).getJSONArray(Commons.OUTPUT).getJSONObject(0)
                            .optString(Commons.PROD_NUM);
                }
            }
            else {
                producerCodeChubb = enrichedJson.getJSONObject(Commons.PRODUCER_SEARCH_RESPONSE).getJSONArray(Commons.OUTPUT).getJSONObject(0)
                        .optString(Commons.PROD_NUM);
            }
        } catch (JSONException e) {
            logger.error("--------------- ERROR WHILE GETTING CHUBB PRODUCER CODE --------" + e);
        } catch (Exception e) {
            logger.error("--------------- EXCEPTION WHILE GETTING CHUBB PRODUCER CODE --------" + e);
        }
        return producerCodeChubb;
    }

    public String getExtractedProducerChubbCode(final JSONObject jsonObj) {
        String producerChubbCode = Commons.EMPTY_STRING;
        try {
            producerChubbCode = jsonObj.getJSONObject("ingestionData")
                    .getJSONObject("producer")
                    .optString("producerNumberChubb");

        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE GETTING PROD ACE CODE IN JSON---" + e);
        } catch (Exception e) {
            logger.error("--------------- EXCEPTION WHILE GETTING ACE PRODUCER CODE --------" + e);
        }
        return producerChubbCode;
    }

    public String getExtractedProducerChubbSubCode(final JSONObject jsonObj) {
        String producerChubbCode = Commons.EMPTY_STRING;
        try {
            producerChubbCode = jsonObj.getJSONObject("ingestionData")
                    .getJSONObject("producer")
                    .optString("subNumber");

        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE GETTING PROD ACE CODE IN JSON---" + e);
        } catch (Exception e) {
            logger.error("--------------- EXCEPTION WHILE GETTING ACE PRODUCER CODE --------" + e);
        }
        return producerChubbCode;
    }

    private JSONObject updateACEProducerCode(JSONObject jsonobj, String producerCodeACE) {
        logger.info("-----UPDATING ACE PRODUCER CODE-----");
        JSONObject updatedObj = jsonobj;

        try {
            updatedObj.getJSONObject(Commons.INGESTION_DATA)
                    .getJSONObject(Commons.PRODUCER)
                    .put(Commons.PROD_NUM_ACE, producerCodeACE);
        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE UPDATING LEGACY ACE CODE IN JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateChubbProducerCode(JSONObject jsonobj, String enrichedProducerCodeChubb) {
        logger.info("-----UPDATING CHUBB PRODUCER CODE-----");
        JSONObject updatedObj = jsonobj;
        try {
            updatedObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.PRODUCER)
                    .put(Commons.PROD_NUM_CHUBB, enrichedProducerCodeChubb);
        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE UPDATING CHUBB PRODUCER CODE IN JSON---" + e);
        }
        return updatedObj;
    }

    private String getProdSubcodeField(JSONObject enrichedJson) {
        String prodSubCode = Commons.EMPTY_STRING;
        try {
            prodSubCode = enrichedJson.getJSONObject("producerSearchResponse").getJSONArray("output").getJSONObject(0)
                    .optString("subProdNum");
        } catch (Exception e) {
            logger.error("--------------- ERROR WHILE GETTING PROD SUB CODE --------" + e);
        }
        return prodSubCode;
    }

    private String getBusinessSegmentField(JSONObject enrichedJson) {
        String businessSeg = Commons.EMPTY_STRING;
        try {
            businessSeg = enrichedJson.getJSONObject("insuredMatchingResponse").getJSONArray("output").getJSONObject(0)
                    .getJSONObject("businessDetails").optString("businessSegment");
        } catch (Exception e) {
            logger.error("--------------- ERROR WHILE GETTING BUSINESS DETAILS --------" + e);
        }
        return businessSeg;
    }

    private String getWebsiteUrlField(JSONObject enrichedJson) {
        String website = Commons.EMPTY_STRING;
        try {
            website = enrichedJson.getJSONObject("insuredMatchingResponse").getJSONArray("output").getJSONObject(0)
                    .getJSONArray("cosmosDocuments").getJSONObject(0).getJSONObject("website")
                    .optString("value");
        } catch (Exception e) {
            logger.error("--------------- ERROR WHILE GETTING WEBSITE URL --------" + e);
        }
        return website;
    }

    private String getAgencyLicensingField(JSONObject enrichedJson, JSONObject standardizedJson) {
        String submissionNumber = getSubmissionNumberFromInputJson(standardizedJson);
        String agencyLic = "N";
        try {
            JSONArray writingCompanies = enrichedJson.getJSONObject("producerSearchResponse")
                    .getJSONArray("output").getJSONObject(0).getJSONArray("writingCompany");
            String state = getStateFromJson(standardizedJson);

            for(int i=0; i<writingCompanies.length(); i++) {
                String company = writingCompanies.getJSONObject(i).optString("wrtngCompName");
                String status = writingCompanies.getJSONObject(i).optString("compStatDesc");
                if(LICENSING_COMPANY.equalsIgnoreCase(company) && LICENSING_STATUS.equalsIgnoreCase(status)) {
                    JSONArray appointments = writingCompanies.getJSONObject(i).getJSONArray("appointment");
                    boolean stateMatched = false;
                    for(int j=0; j<appointments.length(); j++){
                        if (state.equalsIgnoreCase(appointments.getJSONObject(j).optString("licStCode"))){
                            agencyLic = "Y";
                            stateMatched = true;
                            break;
                        }

                    }
                    if(stateMatched){
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(Commons.logStringFormat("----- EXCEPTION WHILE GETTING AGENCY LICENSE -----submissionNumber ::"+submissionNumber, Commons.getStackTraceAsString(e)));

        }

        return agencyLic;
    }

    private String getAgencyMDMIDField(JSONObject enrichedJson) {
        String agencyMM = Commons.EMPTY_STRING;
        try {
            agencyMM = enrichedJson.getJSONObject("producerSearchResponse").getJSONArray("output").getJSONObject(0)
                    .optString("busnPartyGuid");
        } catch (Exception e) {
            logger.error("--------------- ERROR WHILE GETTING AGENCY MDMID --------" + e);
        }
        return agencyMM;
    }

    private String getInsuredFeinField(JSONObject enrichedJson) {
        String insuredFein = Commons.EMPTY_STRING;
        try {
            insuredFein = enrichedJson.getJSONObject("insuredMatchingResponse").getJSONArray("output").getJSONObject(0)
                    .getJSONObject("businessDetails").getJSONObject("fein").optString("value");
        } catch (Exception e) {
            logger.error("--------------- ERROR WHILE GETTING INSURED FIEN --------" + e);
        }
        return insuredFein;
    }

    private String getOwnershipField(JSONObject enrichedJson) {
        String ownership = Commons.EMPTY_STRING;
        try {
            ownership = enrichedJson.getJSONObject("insuredMatchingResponse").getJSONArray("output").getJSONObject(0)
                    .getJSONObject("businessDetails").getJSONObject("ownership").optString("value");
        } catch (Exception e) {
            logger.error("--------------- ERROR WHILE GETTING OWNERSHIP --------" + e);
        }
        return ownership;
    }

    private String getCuwField(JSONObject finalJson, JSONObject enrichedJson) {
        String cuw = "";
        try {
            cuw = finalJson.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0)
                    .getJSONObject("partyIdentity")
                    .optString("typeId");
        } catch (Exception e) {
            logger.info("----- CUW IS NOT EXTRACTED. GETTING IT FROM ENRICHMENT -----");
        }

        if(cuw.isBlank()) {
            try {
                cuw = enrichedJson
                        .getJSONObject("insuredMatchingResponse")
                        .getJSONArray("output").getJSONObject(0)
                        .getJSONObject("mdmDetails")
                        .optString("wipInsuredNumber");
            } catch (Exception e) {
                logger.error("--------------- ERROR WHILE GETTING CUW FROM ENRICHMENT --------" + e);
            }
        }
        return cuw;
    }

    public Long getIncident(JSONObject message) {
        Long incidentId = 0L;
        try {
            String sourceNumber = message.optString("source_reference_number");
            String incident[] = sourceNumber.split("/");
            if(!incident[0].isBlank()) {
                incidentId = Long.parseLong(incident[0]);
            }
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE GETTING INCIDENT ID -----" + e);
        }
        return incidentId;
    }

    public String getIncidentReference(JSONObject message) {
        String incidentId = "";
        try {
            String sourceNumber = message.optString("source_reference_number");
            String incident[] = sourceNumber.split("/");
            if(!incident[1].isBlank()) {
                incidentId = incident[1];
            }
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE GETTING INCIDENT ID -----" + e);
        }
        return incidentId;
    }

    public Long getIncidentFromKafkaMessage(JSONObject message) {
        Long incidentId = 0L;
        try {
            String sourceNumber = message.getJSONObject("source_context").optString("source_reference_number");
            String incident[] = sourceNumber.split("/");
            if(!incident[0].isBlank()) {
                incidentId = Long.parseLong(incident[0]);
            }
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE GETTING INCIDENT ID -----" + e);
        }
        return incidentId;
    }

    public String getIncidentRefNoFromKafkaMessage(JSONObject message) {
        String incidentId = Commons.EMPTY_STRING;
        try {
            String sourceNumber = message.getJSONObject("source_context").optString("source_reference_number");
            String incident[] = sourceNumber.split("/");
            if(StringUtils.isNotBlank(incident[1])) {
                incidentId = incident[1];
            }
        } catch (JSONException e) {
            logger.error("----- EXCEPTION WHILE GETTING INCIDENT REFERENCE NUMBER -----" + e.getMessage());
        }
        return incidentId;
    }

    public String getSIMRefAndIncidentNoFromKafkaMessage(JSONObject message) {
        String sourceNumber = Commons.EMPTY_STRING;
        try {
            sourceNumber = message.getJSONObject("source_context").optString("source_reference_number");
        } catch (JSONException e) {
            logger.error("----- EXCEPTION WHILE GETTING INCIDENT REFERENCE NUMBER -----" + e.getMessage());
        }
        return sourceNumber;
    }

    public String getDocumentLinkFromKafkaMessage(JSONObject message) {
        String documentLink = Commons.EMPTY_STRING;
        try {
            documentLink = getDocumentsInfoFromJson(message).getJSONObject(0).optString(Commons.URI_FIELD);
        } catch (JSONException e) {
            logger.error("----- EXCEPTION WHILE GETTING DOCUMENT LINK -----" + e.getMessage());
        }
        return documentLink;
    }

    public JSONObject updateDocumentLinkInInput(JSONObject input, String documentLink) {
        JSONObject inputJsonObj = input;
        try {
            inputJsonObj.getJSONObject("ingestionData")
                    .getJSONObject("messageInformation").putOpt("documentLink", documentLink);
        } catch (JSONException e) {
            logger.error("----- EXCEPTION WHILE UPDATING DOCUMENT LINK -----" + e.getMessage());
        }
        return inputJsonObj;
    }

    public JSONObject updateQueueNameInInput(JSONObject input, String queueName) {
        JSONObject inputJsonObj = input;
        try {
            inputJsonObj.getJSONObject("ingestionData")
                    .getJSONObject("messageInformation").putOpt("queueName", queueName);
        } catch (JSONException e) {
            logger.error("----- EXCEPTION WHILE UPDATING QUEUE NAME -----" + e.getMessage());
        }
        return inputJsonObj;
    }

    public JSONObject updateBrokerAPIFlag(JSONObject input, boolean isBrokerAPISubmission) {
        JSONObject inputJsonObj = input;
        try {
            inputJsonObj.getJSONObject("ingestionData")
                    .getJSONObject("messageInformation").put("isBrokerAPISubmission", isBrokerAPISubmission);
        } catch (JSONException e) {
            logger.error("----- EXCEPTION WHILE UPDATING BROKER API SUBMISSION FLAG -----" + e.getMessage());
        }
        return inputJsonObj;
    }

    public String getSTPFlag(JSONObject kafkaMessage) {
        String stp = Commons.EMPTY_STRING;
        String submissionNumber = getSubmissionNumberFromJson(kafkaMessage);
        logger.info("------- SUBMISSION: {} :GETTING STP -----", submissionNumber);
        try {
            stp = kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString("stp");
        } catch (JSONException e) {
            logger.warn("------ SUBMISSION: {} :JSON EXCEPTION WHILE GETTING STP VALUE FROM KAFKA MESSAGE: {} --------", submissionNumber, e);
        } catch (Exception e) {
            logger.warn("------ SUBMISSION: {} :EXCEPTION WHILE GETTING STP VALUE FROM KAFKA MESSAGE : {} --------", submissionNumber, e);
        }
        logger.info("------- SUBMISSION: {} :STP VALUE: {}----------", submissionNumber, stp);
        return stp;
    }

    public JSONObject updateSTPFlag(JSONObject kafkaMessage, JSONObject inputJson) {
        String stp = getSTPFlag(kafkaMessage);
        JSONObject updatedJson = inputJson;
        String submissionNumber = getSubmissionNumberFromJson(kafkaMessage);
        logger.info("------- SUBMISSION: {} :SETTING STP: {} -----", submissionNumber, stp);
        try {
            updatedJson.getJSONObject("ingestionData")
                    .getJSONObject("messageInformation").putOpt("stp", stp);
        } catch (JSONException e) {
            logger.warn("------ SUBMISSION: {} :JSON EXCEPTION WHILE SETTING STP VALUE IN KAFKA MESSAGE: {} --------", submissionNumber, e);
        } catch (Exception e) {
            logger.warn("------ SUBMISSION: {} :EXCEPTION WHILE SETTING STP VALUE IN KAFKA MESSAGE: {} --------", submissionNumber, e);
        }
        logger.info("------- SUBMISSION: {} :STP VALUE UPDATE IN JSON ---------", submissionNumber);
        return updatedJson;
    }

    private String getRevenueField(final JSONObject finalJson, final JSONObject enrichedJson) {
        String submissionNumber = getSubmissionNumberFromInputJson(finalJson);
        String annualRevenue = getStringValueFromInsuredSection(finalJson, FieldConstants.REVENUE_FIELD);
        if (StringUtils.isNotBlank(annualRevenue)) {
            logger.info("------- SUBMISSION: {} :ANNUAL REVENUE IS EXTRACTED : {}------", submissionNumber, annualRevenue);
            return annualRevenue;
        }
//		logger.debug("-------ANNUAL REVENUE NOT EXTRACTED.CHECKING FOR TOTAL SALES------");
//		String totalSales = getStringValueFromPropertySection(finalJson, FieldConstants.TOTAL_SALES_FIELD);
//		if (StringUtils.isNotBlank(totalSales)) {
//			logger.info("-------SUBMISSION: {} :TOTAL SALES IS EXTRACTED : {}------", submissionNumber, totalSales);
//			return totalSales;
//		}
//		logger.debug("-------TOTAL SALES EXTRACTED.CHECKING FOR ENRICHED REVENUE------");
        String enrichedRevenue = getStringValueFromEnrichedBusinessDetails(enrichedJson, FieldConstants.ENRICHED_REVENUE_FILED);
        if (StringUtils.isNotBlank(enrichedRevenue)) {
            logger.info("------- SUBMISSION: {} :ENRICHED REVENUE IS AVAILABLE : {}------", submissionNumber, enrichedRevenue);
            return enrichedRevenue;
        }
        logger.info("------- SUBMISSION: {} :NO REVENUE EXTRACTED OR ENRICHED; RETURNING EMPTY-------", submissionNumber);
        return Commons.EMPTY_STRING;
    }

    public String getStringValueFromInsuredSection(final JSONObject obj, final String fieldName) {
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        String value = Commons.EMPTY_STRING;
        try {
            value = obj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0)
                    .optString(fieldName);
        } catch (JSONException e) {
            logger.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE GETTING {} FROM JSON   -----", submissionNumber, fieldName);
        }
        catch (Exception e) {
            logger.warn("-----SUBMISSION: {} :EXCEPTION WHILE GETTING {} FROM JSON  -----", submissionNumber, fieldName);
        }
        return value;
    }

    public String getStringValueFromPropertySection(final JSONObject obj, final String fieldName) {
        String value = Commons.EMPTY_STRING;
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {
            value = obj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("property")
                    .optString(fieldName);
        } catch (JSONException e) {
            logger.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE GETTING {} FROM JSON   -----", submissionNumber, fieldName);
        }
        catch (Exception e) {
            logger.warn("----- SUBMISSION: {} :EXCEPTION WHILE GETTING {} FROM JSON  -----", submissionNumber, fieldName);
        }
        return value;
    }

    public String getStringValueFromEnrichedBusinessDetails(final JSONObject obj, final String fieldName) {
        String value = Commons.EMPTY_STRING;
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {
            value = obj.getJSONObject("insuredMatchingResponse")
                    .getJSONArray("output").getJSONObject(0)
                    .getJSONObject("businessDetails")
                    .getJSONObject(fieldName)
                    .optString("value");
        } catch (JSONException e) {
            logger.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE GETTING {} FROM ENRICHED JSON   -----", submissionNumber, fieldName);
        }
        catch (Exception e) {
            logger.warn("----- SUBMISSION: {} :EXCEPTION WHILE GETTING {} FROM ENRICHED JSON  -----", submissionNumber, fieldName);
        }
        return value;
    }

    private String getNAICSField(JSONObject enrichedJson) {
        String ownership = Commons.EMPTY_STRING;
        try {
            ownership = enrichedJson.getJSONObject("insuredMatchingResponse").getJSONArray("output").getJSONObject(0)
                    .getJSONObject("businessDetails")
                    .getJSONObject("naicsCode")
                    .optString("value");
        } catch (Exception e) {
            logger.error("--------------- ERROR WHILE GETTING NAICS --------" + e);
        }
        return ownership;
    }

    private JSONObject updateNAICS(JSONObject obj, String naics) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("submission").getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0).put("NAICS", naics);
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE UPDATING NAICS IN JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateProdSubcode(JSONObject obj, String prodSubcode) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("producer").put("subNumber", prodSubcode);
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE UPDATING PROD SUB CODE IN JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateBusinessSegment(JSONObject obj, String businessSegment) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("submission").getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0).put("organizationTypeCode", businessSegment);
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE UPDATING BUSINESS SEGMENT IN JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateWebsiteURL(JSONObject obj, String websiteURL) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("submission").getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0).getJSONObject("communication")
                    .put("websiteUrl", websiteURL);
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE UPDATING WEBSITE URL IN JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateCuw(JSONObject obj, String cuw) {
        JSONObject updatedObj = obj;
        try {
            JSONObject insuredObj = updatedObj.getJSONObject("ingestionData").getJSONObject("submission").getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0);
            JSONObject partyIdentity = insuredObj.getJSONObject("partyIdentity");
            partyIdentity.put("typeId", cuw);
        } catch (JSONException e) {
            logger.error("----- EXCEPTION WHILE GETTING THE INSURED OBJECT -----" + e);
        }
        return updatedObj;
    }

    private JSONObject updateRevenue(JSONObject obj, String revenue) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0)
                    .put("totalRevenueAmount", revenue);
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE UPDATING REVENUE IN JSON -----" + e);
        }
        return updatedObj;
    }

    private JSONObject updateInsuredAddress(JSONObject obj, JSONObject enrichedJson) {
        JSONObject updatedObj = obj;

        try {
            JSONArray insuredObj = updatedObj.getJSONObject("ingestionData").getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0).getJSONArray("insured");
            JSONArray address = insuredObj.getJSONObject(0).getJSONArray("address");

            if (StringUtils.isNotBlank(address.getJSONObject(0).optString("line1")) || StringUtils
                    .isNotBlank(address.getJSONObject(0).getJSONObject("detailAddress").optString("POBox"))) {
                return updatedObj;
            }
            JSONArray insuredMatchingOutputArr = enrichedJson.getJSONObject("insuredMatchingResponse")
                    .getJSONArray("output");
            if (null != insuredMatchingOutputArr && insuredMatchingOutputArr.length() > 0) {
                JSONObject insuredAddress = insuredMatchingOutputArr.getJSONObject(0)
                        .getJSONArray("locationDetails").getJSONObject(0);

                String streetAddr1 = insuredAddress.getJSONObject("address1").optString("value");
                String streetAddr2 = insuredAddress.getJSONObject("address2").optString("value");
                String city = insuredAddress.getJSONObject("city").optString("value");
                String state = insuredAddress.getJSONObject("state").optString("value");
                String zip = insuredAddress.getJSONObject("zip").optString("value");

                JSONObject stdInsuredObj = updatedObj.getJSONObject("ingestionData").getJSONObject("submission")
                        .getJSONArray("policy").getJSONObject(0)
                        .getJSONArray("insured").getJSONObject(0)
                        .getJSONArray("address").getJSONObject(0);

                if(StringUtils.isNotBlank(streetAddr1)) {
                    stdInsuredObj.put("line1", streetAddr1);
                    stdInsuredObj.put("line2", streetAddr2);
                    stdInsuredObj.put("city", city);
                    stdInsuredObj.put("stateOrProvinceCode", state);
                    stdInsuredObj.put("postalCode", zip);
                }
            }
        } catch (Exception e) {
            logger.info("----- EXCEPTION WHILE UPDATING THE INSURED ADDRESS -----");
        }
        return updatedObj;
    }

    private JSONObject updateAgencyLicensing(JSONObject obj, String agencyLicensing) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("producer").getJSONObject("questionAnswer")
                    .put("answerCode", agencyLicensing);
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE UPDATING AGENCY LICENSING IN JSON---" + e);
        }
        return updatedObj;
    }

    public String getAgencyLicensing(JSONObject obj) {
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        String agencyLicense = Commons.EMPTY_STRING;
        try {
            agencyLicense = obj.getJSONObject("ingestionData").getJSONObject("producer").getJSONObject("questionAnswer")
                    .optString("answerCode");
        } catch (JSONException e) {
            logger.error("--- SUBMISSION :{} :JSON EXCEPTION WHILE UPDATING AGENCY LICENSING IN JSON : {} ---", submissionNumber, e);
        }
        catch (Exception e) {
            logger.error("---SUBMISSION:{} :EXCEPTION WHILE UPDATING AGENCY LICENSING IN JSON: {}---", submissionNumber, e);
        }
        return agencyLicense;
    }

    private JSONObject updateAgencyMDMID(JSONObject obj, String agencyMDMID) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("producer").getJSONObject("partyIdentity").put("typeId",
                    agencyMDMID);
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE UPDATING AGENCY MDMID IN JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateInsurredFEIN(JSONObject obj, String insuredFEIN) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("submission").getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0).put("FEIN", insuredFEIN);
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE UPDATING INSURED FEIN IN JSON---" + e);
        }
        return updatedObj;
    }

    private JSONObject updateOwnership(JSONObject obj, String ownership) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("messageInformation").put("ownership", ownership);
        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE UPDATING OWNERSHIP IN JSON---" + e);
        }
        return updatedObj;
    }

    public String getSubmissionNumberFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString(Commons.SUBMISSION_NUMBER_FIELD);
    }

    public String getPriorityFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString(Commons.PRIORITY);
    }

    public JSONArray getDocumentsInfoFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).getJSONArray(Commons.DOCUMENTS_FIELD);
    }

    public String getSourceReferenceNumFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString(Commons.SOURCE_SYSTEM_REF_NUMBER_FIELD);
    }

    public String getChannelFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString(Commons.CHANNEL_FIELD);
    }

    public String getSourceReferenceJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString(Commons.SOURCE_REFERENCE_JSON);
    }

    public String getSourceSystemIdFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString(Commons.SOURCE_SYSTEM_ID_FIELD);
    }

    public String getUseCaseIdFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.USECASE_ID_FIELD);
    }

    public String getStatusFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.STATUS_FIELD);
    }

    public String getRegionFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.REGION_FIELD);
    }

    public String getCountryFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.COUNTRY_FIELD);
    }

    public String getLobFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.LOB_FIELD);
    }

    public String getProductFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.PRODUCT_FIELD);
    }

    public String getMessageFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).optString(Commons.MESSAGE_FIELD);
    }

    public Long getNoOfSubmittedDocsFromJson(JSONObject kafkaMessage) {
        return kafkaMessage.getJSONObject(Commons.INGESTION_CONTEXT_FIELD).getLong(Commons.NO_OF_DOCS_SUBMITTED_FIELD);
    }


    public String appendEnrichedDataInJson(String standardizedJson, String enrichedJson, String kafkaMessage) {
        logger.info("----APPENDING ENRICHMENT JSON TO STANDARDIZED JSON-----");
        JsonObject jsonObject = jsonb.fromJson(standardizedJson, JsonObject.class);
        JsonObject enrichObject = jsonb.fromJson(enrichedJson, JsonObject.class);
        JsonObject kafkaObject = jsonb.fromJson(kafkaMessage, JsonObject.class);
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder(jsonObject);
        jsonObjectBuilder.add(Commons.ENRICHED_FIELD, enrichObject);
        jsonObjectBuilder.add(Commons.KAFKA_MESSAGE, kafkaObject);
        JsonObject updatedJsonObject = jsonObjectBuilder.build();
        String updatedstandardizedJsonString = jsonb.toJson(updatedJsonObject);
        logger.info("-----ENRICHED JSON HAS BEEN APPENDED TO STANDARD JSON-------");
        return updatedstandardizedJsonString;
    }

    public String getProducerCodeFromJSON(JSONObject jsonObj) {
        try {
            return jsonObj.getJSONObject("ingestionData")
                    .getJSONObject("producer")
                    .optString("producerNumber");
        } catch (JSONException e) {
            logger.error("-----EXCEPTION WHILE GETTING PRODUCER CODE FROM JSON-----" + e);
            return Commons.EMPTY_STRING;
        } catch (Exception e) {
            logger.error("---EXCEPTION WHILE GETTING PRODUCER CODE FROM JSON---" + e);
            return Commons.EMPTY_STRING;
        }
    }

    public String getFinalProducerNumber(JSONObject jsonObj) {
        logger.info("----- GET FINAL PRODUCER NUMBER FOR ENRICHMENT -----");
        String finalProducerCode = Commons.EMPTY_STRING;
        String extractedChubbProducer = getExtractedProducerChubbCode(jsonObj);
        String extractedProducerNumber = getProducerCodeFromJSON(jsonObj);
        if (StringUtils.isNotBlank(extractedChubbProducer)) {
            finalProducerCode = extractedChubbProducer;
        } else if (StringUtils.isNotBlank(extractedProducerNumber)) {
            finalProducerCode = extractedProducerNumber;
        }
        logger.info("------FINAL PRODUCER NUMBER---------" + finalProducerCode);
        return finalProducerCode;
    }

    public String getInsuredName(JSONObject jsonObject) {
        String insuredName = Commons.EMPTY_STRING;
        try {
            insuredName = jsonObject.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0)
                    .optString("fullName");
        } catch (JSONException e) {
            logger.warn("----- EXCEPTION WHILE GETTING THE INSURED NAME -----" + e);
        }

        return insuredName;
    }

    public String getProducerState(JSONObject jsonObject) {
        String state = Commons.EMPTY_STRING;
        try {
            JSONArray addressArray = jsonObject.getJSONObject("ingestionData")
                    .getJSONObject("producer").getJSONArray("address");

            if(addressArray != null && !addressArray.isEmpty()){
                for (int i=0; i<addressArray.length(); i++) {
                    JSONObject addressObj = addressArray.getJSONObject(i);
                    if(addressObj.optString("typeCode").equalsIgnoreCase("Agency")){
                        state = addressObj.optString("stateOrProvinceCode");
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            logger.warn("----- EXCEPTION WHILE GETTING PRODUCER STATE -----" + e);
        }

        return state;
    }

    public JSONObject updateProducerState(JSONObject obj, String mappedState) {
        JSONObject updatedObj = obj;
        try {
            JSONArray addressArray = updatedObj.getJSONObject("ingestionData")
                    .getJSONObject("producer").getJSONArray("address");
            if(addressArray != null && !addressArray.isEmpty()){
                for (int i=0; i<addressArray.length(); i++) {
                    JSONObject addressObj = addressArray.getJSONObject(i);
                    if(addressObj.optString("typeCode").equalsIgnoreCase("Agency")){
                        addressObj.put("stateOrProvinceCode", mappedState);
                        break;
                    }
                }
            }
        } catch (JSONException e) {
            logger.error("---EXCEPTION WHILE UPDATING PRODUCER STATE IN JSON---" + e);
        }
        return updatedObj;
    }

    public String getSubmissionNumberFromInputJson(JSONObject obj) {
        String submissionNumber = Commons.EMPTY_STRING;
        try {
            submissionNumber = obj.getJSONObject("metadata")
                    .optString("submissionNumber");
        } catch (Exception e) {
            logger.warn("----- EXCEPTION WHILE GETTING SUBMISSION NUMBER FROM INPUT JSON : {} ------", e);
        }
        return submissionNumber;
    }

    public int getStandardizedNumberOfLocationFromInputJson(JSONObject obj) {
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        int locationCount = 0;
        try {
            locationCount = obj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("property")
                    .optInt("uniqueLocationCount");
        } catch (JSONException e) {
            logger.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE GETTING STANDARDIZED NUMBER OF LOCATION FROM INPUT JSON : {} -----",
                    submissionNumber, e);
        }
        return locationCount;
    }

    public String getStateFromJson(JSONObject standardizedJson){
        String state = standardizedJson.getJSONObject("ingestionData").getJSONObject("submission")
                .getJSONArray("policy").getJSONObject(0)
                .getJSONArray("insured").getJSONObject(0)
                .getJSONArray("address").getJSONObject(0)
                .optString("stateOrProvinceCode");
        return state;
    }
    public Double getRevenue(JSONObject dataProductObject) {
        Double revenue = 0D;
        String submissionNumber = getSubmissionNumberFromInputJson(dataProductObject);

        try {

            revenue = dataProductObject.getJSONObject("ingestionData").getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0).getJSONArray("insured").getJSONObject(0)
                    .getDouble("totalRevenueAmount");
        } catch (JSONException e) {
            logger.warn(Commons.logStringFormat("----- EXCEPTION WHILE GETTING REVENUE AS Double FROM JSON -----submissionNumber:: "+submissionNumber +" ", Commons.getStackTraceAsString(e)));
        }

        if (revenue == 0D) {
            try {
                String revenueString = dataProductObject.getJSONObject("ingestionData").getJSONObject("submission")
                        .getJSONArray("policy").getJSONObject(0).getJSONArray("insured").getJSONObject(0)
                        .optString("totalRevenueAmount");
                if (!revenueString.isBlank())
                    revenue = Double.parseDouble(revenueString.replaceAll(",", ""));
            } catch (JSONException e) {
                logger.warn(Commons.logStringFormat("----- EXCEPTION WHILE GETTING REVENUE AS String FROM JSON -----submissionNumber ::"+submissionNumber, Commons.getStackTraceAsString(e)));
            }
        }
        return revenue;
    }

    //D8INGEST-6905
    private String getClimateTechIndicator(JSONObject jsonObj, JSONObject enrichedJson) {
        String indicator = Commons.EMPTY_STRING;
        try {
            indicator = jsonObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.CLIMATE_TECH).optString(Commons.CLIMATE_TECH_INDICATOR);
        } catch (JSONException e) {
            logger.error("--------------- ERROR WHILE GETTING CLIMATE TECH INDICATOR --------" + e);
        } catch (Exception e) {
            logger.error("--------------- EXCEPTION WHILE GETTING CLIMATE TECH INDICATOR --------" + e);
        }

        return indicator;
    }

    public boolean kickoutMajorAccountSubmissions(String standardizedInput) {

        JSONObject dataProductObject = null;
        Double revenue = 0D;
        String submissionNumber = getSubmissionNumberFromInputJson(dataProductObject);
        try {
            logger.debug("-------CALLING  KICKOUT MAJORACCOUNT SUBMISSIONS -----");

            dataProductObject = new JSONObject(standardizedInput);

            if (dataProductObject != null && !dataProductObject.isEmpty()) {
                revenue = getRevenue(dataProductObject);
            }
            if (revenue >= Commons.BILLION) {
                logger.debug(Commons.logStringFormat("-------SUBMISSION IS A MAJORACCOUNT ----- ",submissionNumber));
                return true;
            } else {
                logger.debug(Commons.logStringFormat("-------SUBMISSION IS NOT A MAJORACCOUNT -----",submissionNumber));
                return false;
            }
        } catch (Exception e) {
            logger.warn(Commons.logStringFormat("---ERROR WHILE KICKOUT MAJORACCOUNT SUBMISSIONS---- submissionNumber ::"+submissionNumber, Commons.getStackTraceAsString(e)));
            return false;

        }
    }

    public JSONArray getEnrichedSicCodeObject(JSONObject dataProductObject) {
        JSONArray sicCodeObject = null;

        try {
            sicCodeObject = dataProductObject.getJSONObject("enrichedData")
                    .getJSONObject("insuredMatchingResponse")
                    .getJSONArray("output").getJSONObject(0)
                    .getJSONArray("enrichedSicCode");
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE GETTING THE ENRICHED SIC CODE OBJECT -----", e);
        }

        return sicCodeObject;
    }

    // D8INGEST-6939

    public String getEmailSubjectFromMessage(String intakeMessage) {
        JSONObject obj = new JSONObject(intakeMessage);
        String submissionNumber = getSubmissionNumberFromJson(obj);
        String emailSubject = "";
        try {
            emailSubject = obj.getJSONObject(Commons.SOURCE_CONTEXT_FIELD).optString("email_subject");

        } catch (JSONException e) {
            logger.warn(Commons.logStringFormat("---ERROR WHILE GETTING EMAIL SUBJECT FOR CISA ---- submissionNumber ::" + submissionNumber, Commons.getStackTraceAsString(e)));
        }
        return emailSubject;
    }

    public String deriveCISAIdentifier(String standardizationJson, final String intakeMessage) {
        String isCISA = Commons.N;
        JSONObject obj = new JSONObject(standardizationJson);
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {
            String emailSubject = getEmailSubjectFromMessage(intakeMessage);
            if (emailSubject.toLowerCase().contains("cisa")) {
                isCISA = Commons.Y;
            }

        } catch (JSONException e) {
            logger.warn(Commons.logStringFormat("---ERROR WHILE GETTING EMAIL SUBJECT FOR CISA IDENTIFIER ---- submissionNumber ::" + submissionNumber, Commons.getStackTraceAsString(e)));
        }
        return isCISA;
    }

    public String getCISAIdentifier(final String standardizationJson) {
        JSONObject obj = new JSONObject(standardizationJson);
        String cisaIdentifier = Commons.N;
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {
            cisaIdentifier = obj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.CISA).optString(Commons.CISA_IDENTIFIER_FIELD);
        } catch (JSONException e) {
            logger.warn(Commons.logStringFormat("---EXCEPTION WHILE GETTING CISA BLOCK IN JSON ---- submissionNumber ::" + submissionNumber, Commons.getStackTraceAsString(e)));
        }
        return cisaIdentifier;
    }

    public String updateCISAIdentifier(String standardizationJson, String intakeMessage, String cisaIdentifier) {
        JSONObject updatedObj = new JSONObject(standardizationJson);
        String submissionNumber = getSubmissionNumberFromInputJson(updatedObj);
        try {
            Gson gson = new Gson();
            CisaAttributes cisa = new CisaAttributes();
            cisa.setCisaIdentifier(Commons.N);
            if (cisaIdentifier.equalsIgnoreCase(Commons.Y)) {
                cisa.setCisaSubmission();
                // Update Producer Number and SubCode
                String extractedProducerNumberChubb = getExtractedProducerNumberChubb(standardizationJson, intakeMessage);
                String extractedProducerSubNumber = getExtractedProducerSubCode(standardizationJson, intakeMessage);
                updatedObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.PRODUCER).put(Commons.PROD_NUM_CHUBB, extractedProducerNumberChubb);
                updatedObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.PRODUCER).put(Commons.SUB_NUMBER, extractedProducerSubNumber);
                logger.info("---------------------- CISA SUBMISSION: " + submissionNumber + " -------------------------- EXTRACTED PRODUCER CODE: " + extractedProducerNumberChubb + "-----------" + extractedProducerSubNumber);

            }
            updatedObj.getJSONObject(Commons.INGESTION_DATA).put(Commons.CISA, new JSONObject(gson.toJson(cisa)));
        } catch (JSONException e) {
            logger.warn(Commons.logStringFormat("---EXCEPTION WHILE UPDATING CISA IDENTIFIER IN JSON ---- submissionNumber ::" + submissionNumber, Commons.getStackTraceAsString(e)));
        }
        return updatedObj.toString();
    }

    public String getProducerBranch(final JSONObject finalJson) {
        String producerBranch = finalJson.getJSONObject(Commons.INGESTION_DATA)
                .getJSONObject(Commons.PRODUCER)
                .optString(Commons.BRANCH);
        return producerBranch;
    }

    public JSONArray getAllPolicies(final JSONObject finalJson) {
        JSONArray policies = finalJson.getJSONObject(Commons.INGESTION_DATA)
                .getJSONObject(Commons.SUBMISSION).getJSONArray(Commons.POLICY);
        return policies;
    }

    public String getAgencyName(final JSONObject finalJson) {
        String agencyName = finalJson.getJSONObject(Commons.INGESTION_DATA)
                .getJSONObject(Commons.PRODUCER)
                .optString(Commons.FULL_NAME);
        return agencyName;
    }

    // D8INGEST-6837
    private String extractProducerCodeFromSubject(final String standardizationJson, final String intakeMessage) {
        String chubbProducerCode = Commons.EMPTY_STRING;
        JSONObject obj = new JSONObject(standardizationJson);
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {
            String emailSubject = getEmailSubjectFromMessage(intakeMessage);

            chubbProducerCode = regexMatchProducerCode(emailSubject);
            if (chubbProducerCode.isBlank()) {
                String extractedProducerCode = obj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.PRODUCER).optString(Commons.PROD_NUM_CHUBB);
                chubbProducerCode = regexMatchProducerCode(extractedProducerCode);
                if (chubbProducerCode.isBlank()) {
                    chubbProducerCode = extractedProducerCode;
                }
            }

        } catch (JSONException e) {
            logger.warn(Commons.logStringFormat("---ERROR WHILE GETTING EMAIL SUBJECT FOR CISA PRODUCER CODE ---- submissionNumber ::" + submissionNumber, Commons.getStackTraceAsString(e)));
        }
        return chubbProducerCode;
    }

    private String regexMatchProducerCode(String input) {
        String regex = Commons.PRODUCER_CODE_REGEX;
        String chubbProducerCode = Commons.EMPTY_STRING;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            chubbProducerCode = matcher.group();
        }
        return chubbProducerCode;
    }

    public String getExtractedProducerNumberChubb(final String standardizationJson, final String intakeMessage) {
        String producerNumberChubb = "";
        JSONObject obj = new JSONObject(standardizationJson);
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {

            String prodCode = extractProducerCodeFromSubject(standardizationJson, intakeMessage);
            producerNumberChubb = prodCode.split("-")[0];

            if (producerNumberChubb.isBlank()) {
                producerNumberChubb = obj.getJSONObject(Commons.INGESTION_DATA)
                        .getJSONObject(Commons.PRODUCER)
                        .optString(Commons.PROD_NUM_CHUBB);
            }

        } catch (Exception e) {
            logger.warn(Commons.logStringFormat("---ERROR WHILE GETTING CISA PRODUCER CHUBB CODE ---- submissionNumber ::" + submissionNumber, Commons.getStackTraceAsString(e)));
        }

        return producerNumberChubb;
    }

    public String getExtractedProducerSubCode(final String standardizationJson, final String intakeMessage) {
        String prodSubCode = "";
        JSONObject obj = new JSONObject(standardizationJson);
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {

            String prodCode = extractProducerCodeFromSubject(standardizationJson, intakeMessage);
            prodSubCode = (StringUtils.isNotBlank(prodCode) && prodCode.contains("-")) ? prodCode.split("-")[1] : Commons.EMPTY_STRING;

            if (prodSubCode.isBlank()) {
                prodSubCode = obj.getJSONObject(Commons.INGESTION_DATA)
                        .getJSONObject(Commons.PRODUCER)
                        .optString(Commons.SUB_NUMBER);
            }
            if (!prodSubCode.isBlank()) {
                if (prodSubCode.equalsIgnoreCase(Commons.SUB_CODE_999)) {
                    prodSubCode = Commons.SUB_CODE_99999;
                } else {
                    prodSubCode = String.format("%05d", Integer.parseInt(prodSubCode));
                }
            }

        } catch (Exception e) {
            logger.warn(Commons.logStringFormat("---ERROR WHILE GETTING CISA PRODUCER SUB CODE ---- submissionNumber ::" + submissionNumber, Commons.getStackTraceAsString(e)));
        }

        return prodSubCode;
    }

    public JSONObject updateUWUA(final JSONObject policy, String type, String field, String value) {
        JSONObject updatedPolicy = new JSONObject(policy.toString());
        updatedPolicy.getJSONObject(type).put(field, value);
        return updatedPolicy;
    }

    public String getCisaExecutiveEmail(final String finalJson){
        String cisaExecutiveEmail = Commons.EMPTY_STRING;
        JSONObject finalObj = new JSONObject(finalJson);
        String submissionNumber = getSubmissionNumberFromInputJson(finalObj);
        try {
            cisaExecutiveEmail = finalObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.CISA).optString(Commons.CISA_EXECUTIVE_EMAIL);
        } catch (JSONException e) {
            logger.info("---- CISA EXECUTIVE IS NOT DERIVED FOR SUBMISSION: {} -----",submissionNumber);
        }
        return cisaExecutiveEmail;
    }

    public Float getInsureWebsiteUrlFinalScore(final String standardizedJson){
        Float websiteThreshold = 0F;
        JSONObject obj = new JSONObject(standardizedJson);
        String submissionNumber = getSubmissionNumberFromInputJson(obj);
        try {
            JSONArray dataElementSource = obj.getJSONArray(Commons.DATA_ELEMENT_SOURCE);
            for (int i = 0; i < dataElementSource.length(); i++) {
                JSONObject element = dataElementSource.getJSONObject(i);
                if (Commons.DATA_ELEMENT_INSURED_WEBSITE.equalsIgnoreCase(element.optString(Commons.DATA_ELEMENT_NAME))) {
                    websiteThreshold = Float.parseFloat(element.optString(Commons.FINAL_SCORE));
                }
            }
        } catch (Exception e) {
            logger.error("----- ERROR WHILE GETTING THE WEBSITE URL OBJECT FOR SUBMISSION "+ submissionNumber + "-----" + e);
        }
        return websiteThreshold;
    }
}
