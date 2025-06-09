package org.example.helper;

import com.chubb.na.domain.config.DroolsConfig;
import com.chubb.na.domain.data.product.dto.EnrichedFields;
import com.chubb.na.domain.data.product.request.Coverage;
import com.chubb.na.domain.data.product.request.TivEnrichmentRequest;
import com.chubb.na.domain.data.product.response.TivEnrichmentResponse;
import com.chubb.na.domain.drools.SICCodeCheck;
import com.chubb.na.domain.service.RuleApiService;
import com.chubb.na.domain.utils.Commons;
import com.chubb.na.domain.utils.FieldConstants;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.stream.JsonParsingException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

@ApplicationScoped
public class CommonFieldsHelper {

    @Inject
    RuleApiService ruleApiService;

    @Inject
    JsonHelper jsonHelper;

    private DroolsConfig drools = new DroolsConfig();

    private static final Logger log = LoggerFactory.getLogger(CommonFieldsHelper.class);
    HashSet<String> addresses;

    public JSONObject enrichTivAndLocationCount(JSONObject dataProductObject) {
        addresses = new HashSet<>();
        try {
            JSONObject property = dataProductObject.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("property");

            JSONArray locationArray = property.getJSONArray("location");

            //dataProductObject = calculateLocationCount(dataProductObject, locationArray);
            dataProductObject = extractLocationCount(dataProductObject);
            dataProductObject = calculateTiv(dataProductObject, locationArray);
        } catch (JSONException e) {
            log.error("----- EXCEPTION WHILE ENRICHING TIV AND LOCATION COUNT -----" + e.getMessage());
        }

        return dataProductObject;
    }

    private boolean addAddress(JSONObject location) {
        JSONObject addressObj = location.getJSONObject("address");
        String country = addressObj.optString("countryCode");
        String line1 = addressObj.optString("line1");

        if(StringUtils.isBlank(country) || !Commons.COUNTRY_US.equalsIgnoreCase(country) || StringUtils.isBlank(line1)) {
            return false;
        }

        return addresses.add(line1);
    }

    private void removeAddress(JSONObject location) {
        JSONObject addressObj = location.getJSONObject("address");
        String line1 = addressObj.optString("line1");

        addresses.remove(line1);
    }

    private boolean checkTivAggregate(JSONObject location) {
        boolean flag = false;

        try {
            String typeCode = location.getJSONObject("amountItem").optString("typeCode");

            if(StringUtils.isNotBlank(typeCode) && typeCode.equalsIgnoreCase(Commons.TYPE_CODE_AGGREGATE_LIMIT)) {
                String value = location.getJSONObject("amountItem").optString("value");
                if(StringUtils.isNotBlank(value)) {
                    double amount = Double.parseDouble(value.replaceAll(",", ""));
                    if(amount > 0)
                        flag = true;
                }
            }
        } catch (Exception e) {
            log.warn("----- ERROR WHILE GETTING TIV AGGREGATE VALUE: " + e + " -----");
        }

        return flag;
    }

    public JSONObject enrichPayroll(final JSONObject dataProductObject) {
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(dataProductObject);
        JSONObject updatedObj = null;
        if (null != dataProductObject) {
            updatedObj = dataProductObject;
            double payroll = getPayroll(updatedObj);
            log.info("----- SUBMISSION: {} : DERIVED PAYROLL {} -----", submissionNumber, payroll);
            updatedObj = updatePayroll(updatedObj, payroll);
            log.info("----- SUBMISSION: {} : PAYROLL : {} UPDATED IN JSON -----",submissionNumber, payroll);
        }
        return updatedObj;
    }

    private Double getPayroll(final JSONObject obj) {
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(obj);
        Double annualPayroll = getAnnualPayroll(obj);
        if (annualPayroll > Commons.DEFAULT_DOUBLE_VALUE) {
            log.info("----- SUBMISSION: {} :ANNUAL PAYROLL IS EXTRACTED : {} -----", submissionNumber, annualPayroll);
            return annualPayroll;
        }
//        log.debug("----- ANNUAL PAYROLL IS NOT EXTRACTED; CHECKING FOR TOTAL PAYROL ACROSS LOCATIONS ------");
//		Double totalPayrollAcrossLocations = Double
//					.parseDouble(jsonHelper.getStringValueFromPropertySection(obj,
//							FieldConstants.TOTAL_PAYROLL_ACROSS_LOCATIONS).replaceAll(",", ""));
//		if (totalPayrollAcrossLocations > Commons.DEFAULT_DOUBLE_VALUE) {
//			log.info("----- SUBMISSION: {} :TOTAL PAYROLL ACROSS LOCATIONS IS EXTRACTED : {} -----", submissionNumber, totalPayrollAcrossLocations);
//			return totalPayrollAcrossLocations;
//		}
//		log.debug("----- TOTAL PAYROLL ACROSS LOCATIONS IS NOT EXTRACTED; CHECKING FOR ANNUAL REMUNERATION -----");
        Double annualRemuneration = getAnnualRemuneration(obj);
        if (annualRemuneration > Commons.DEFAULT_DOUBLE_VALUE) {
            log.info("----- SUBMISSION: {} :ANNUAL REMUNERATON IS DERIVED : {} -----", submissionNumber, annualRemuneration);
            return annualRemuneration;
        }
        log.info("-----SUBMISSION: {} HAS NO PAYROLL EXTRACTED OR ENRICHED; RETURNING {} -----", submissionNumber, Commons.DEFAULT_DOUBLE_VALUE);
        return Commons.DEFAULT_DOUBLE_VALUE;

    }
    private Double getAnnualRemuneration(final JSONObject obj) {
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(obj);
        Double anuualRemuneration = 0.0;
        try {
            JSONArray annualRemuneration = obj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("workersCompensation")
                    .getJSONObject("business")
                    .getJSONArray("annualRemuneration");

            for(int i=0; i<annualRemuneration.length(); i++) {
                double payroll = Double.parseDouble(annualRemuneration.optString(i).replaceAll(",",""));
                anuualRemuneration += payroll;
            }

        } catch (JSONException e) {
            log.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE GETTING ANNUAL REMUNERATION : {} -----", submissionNumber, e);
        } catch (Exception e) {
            log.warn("----- SUBMISSION: {} :EXCEPTION WHILE GETTING ANNUAL REMUNERATION : {} -----", submissionNumber, e);
        }
        return anuualRemuneration;
    }

    private Double getAnnualPayroll(final JSONObject obj) {
        Double annualPayroll = 0.0;
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(obj);
        try {
            annualPayroll = Double.parseDouble(obj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("workersCompensation")
                    .getJSONObject("business")
                    .optString("totalPayroll").replaceAll(",",""));
        } catch (JSONException e) {
            log.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE GETTING ANNUAL PAYROLL : {} -----", submissionNumber, e);
        } catch (Exception e) {
            log.warn("----- SUBMISSION: {} :EXCEPTION WHILE GETTING ANNUAL PAYROLL : {} -----", submissionNumber, e);
        }
        return annualPayroll;
    }

    private JSONObject updatePayroll(final JSONObject obj, final Double payroll) {
        JSONObject updatedObj = obj;
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(obj);
        try {
            updatedObj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("workersCompensation")
                    .getJSONObject("business")
                    .putOpt("totalPayroll", String.format("%.2f", payroll));
        } catch (JSONException e) {
            log.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE UPDATING PAYROLL IN JSON : {} -----",submissionNumber, e);
        }
        catch (Exception e) {
            log.warn("----- SUBMISSION: {} : EXCEPTION WHILE UPDATING PAYROLL IN JSON : {} -----",submissionNumber, e);
        }
        return updatedObj;
    }

    private JSONObject calculateTiv(JSONObject dataProductObject, JSONArray locationArray) {
        TivEnrichmentRequest request = new TivEnrichmentRequest();

        try {
            for(int i = 0; i < locationArray.length(); i++) {
                JSONObject location = locationArray.getJSONObject(i);

                JSONArray buildingArray = location.getJSONArray("building");
                for(int j = 0; j < buildingArray.length(); j++) {
                    JSONObject building = buildingArray.getJSONObject(j);
                    if(!building.isNull("coverage") && building.getJSONArray("coverage").length() > 0) {
                        JSONArray coverageArray = building.getJSONArray("coverage");
                        for(int k = 0; k < coverageArray.length(); k++) {
                            JSONObject coverageObj = coverageArray.getJSONObject(k);

                            Coverage coverage = new Coverage();
                            coverage.setCoverage(coverageObj.optString("typeCode"));
                            coverage.setLimit(coverageObj.getJSONObject("limit").optString("amount"));

                            request.addCoverage(coverage);
                        }
                    }
                }
            }
        } catch(JSONException e) {
            log.error("----- EXCEPTION WHILE AGGREGATE TIV FROM JSON -----" + e.getMessage());
        }

        TivEnrichmentResponse response = ruleApiService.tivEnrichmentRuleService(request);
        dataProductObject = updateTivInJson(dataProductObject, response);

        return dataProductObject;
    }

    private JSONObject extractLocationCount(JSONObject dataProductObject) {
        JSONObject updatedObj = dataProductObject;
        String submissionNumber = jsonHelper.getSubmissionNumberFromInputJson(dataProductObject);
        try {
            int locationCount = jsonHelper.getStandardizedNumberOfLocationFromInputJson(dataProductObject);

            updatedObj.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("property")
                    .putOpt("locationCount", locationCount);

        } catch (JSONException e) {
            log.warn("----- SUBMISSION: {} :JSON EXCEPTION WHILE UPDATING LOCATION COUNT IN JSON : {} -----",
                    submissionNumber, e);
        } catch (Exception e) {
            log.warn("----- SUBMISSION: {} : EXCEPTION WHILE UPDATING LOCATION COUNT IN JSON : {} -----",
                    submissionNumber, e);
        }
        return updatedObj;
    }

    private JSONObject calculateLocationCount(JSONObject dataProductObject, JSONArray locationArray) {
        int locationCount = 0;

        try {
            for(int i = 0; i < locationArray.length(); i++) {
                JSONObject location = locationArray.getJSONObject(i);

                boolean isUniqueAddress = addAddress(location);

                if(isUniqueAddress) {
                    locationCount++;
                }
            }
        } catch(JSONException e) {
            log.error("----- EXCEPTION WHILE CALCULATING LOCATION COUNT -----" + e.getMessage());
        }

        locationCount = checkForInsuredAddress(dataProductObject, locationCount);

        // Always default the location count to 1
        if(locationCount == 0) {
            locationCount = Commons.DEFAULT_LOCATION_COUNT;
        }
        dataProductObject = updateLocationCountInJson(dataProductObject, locationCount);

        return dataProductObject;
    }

    private int checkForInsuredAddress(JSONObject dataProductObject, int locationCount) {
        try {
            String line1 = dataProductObject.getJSONObject("ingestionData")
                    .getJSONObject("submission")
                    .getJSONArray("policy").getJSONObject(0)
                    .getJSONArray("insured").getJSONObject(0)
                    .getJSONArray("address").getJSONObject(0)
                    .optString("line1");

            if(StringUtils.isNotBlank(line1)) {
                if(addresses.add(line1)) {
                    log.info("----- INSURED ADDRESS LINE 1 IS UNIQUE. ADDING IT TO THE LOCATION COUNT -----");
                    locationCount++;
                }
            }
        } catch (JSONException e) {
            log.error("----- EXCEPTION WHILE CHECKING THE INSURED ADDRESS FOR LOCATION COUNT -----" + e);
        }
        return locationCount;
    }

    private JSONObject updateTivInJson(JSONObject obj, TivEnrichmentResponse response) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData").getJSONObject("submission").getJSONArray("policy")
                    .getJSONObject(0).getJSONObject("property").getJSONObject("limit")
                    .putOpt("amount", response.getTiv());
        } catch (Exception e) {
            log.error("----- EXCEPTION WHILE UPDATING TIV -----" + e.getMessage());
        }
        return updatedObj;
    }

    private JSONObject updateLocationCountInJson(JSONObject obj, int noOfLocations) {
        JSONObject updatedObj = obj;
        try {
            updatedObj.getJSONObject("ingestionData")
                    .getJSONObject("submission").getJSONArray("policy").getJSONObject(0)
                    .getJSONObject("property")
                    .putOpt("locationCount", noOfLocations);
        } catch (Exception e) {
            log.error("----- EXCEPTION WHILE UPDATING LOCATION COUNT -----" + e);
        }
        return updatedObj;
    }

    public String executeSICRules(SICCodeCheck sic, String fileName) {
        String result = Commons.NO;
        KieSession kSession = null;
        FactHandle handle = null;
        try {
            kSession = drools.getKieSession(fileName);
            handle = kSession.insert(sic);
            kSession.fireAllRules();
            kSession.delete(handle);
            kSession.destroy();
        } catch (Exception e) {
            log.error("Exception occured while executing rules: " + e);
        }
        result = StringUtils.isNotBlank(sic.getResult()) ? sic.getResult() : Commons.NO;
        return result;
    }

    /*
     * //D8INGEST-7542 public JSONObject
     * enrichInvidualLicensingProducerNumber(JSONObject dataProductObject, String
     * submissionNumber) { JSONObject individualLicense = new JSONObject(); boolean
     * producerLicense = false; String producerLicenseNumber = Commons.EMPTY_STRING;
     * try {
     *
     * log.info("--- ["
     * +submissionNumber+"] - ENRICHING INDIVIDUAL LICENSENUMBER --- "); if(null!=
     * dataProductObject && dataProductObject.getJSONObject(Commons.ENRICHED_FIELD)
     * .getJSONObject(Commons.PRODUCER_SEARCH_RESPONSE).getJSONArray(Commons.OUTPUT)
     * .getJSONObject(0) .has(Commons.PRODUCER_LICENSE_ENRICHMENT)) {
     *
     * producerLicenseNumber =
     * dataProductObject.getJSONObject(Commons.ENRICHED_FIELD)
     * .getJSONObject(Commons.PRODUCER_SEARCH_RESPONSE).getJSONArray(Commons.OUTPUT)
     * .getJSONObject(0) .optString(Commons.PRODUCER_LICENSE_ENRICHMENT); }else {
     * log.info("["+
     * submissionNumber+"] - Producer_License# FROM PRODUCER MATCHING API Not found:: -- "
     * ); }
     *
     * log.info("["+
     * submissionNumber+"] - LICENSENUMBER FROM PRODUCER MATCHING API IS:: -- "
     * +producerLicenseNumber);
     *
     * } catch (JSONException exe) { log.warn("----- ["
     * +submissionNumber+"] - {} JSON EXCEPTION WHILE GETTING LICENSENUMBER FIELD FROM PRODUCERMATCHING RESPONSE JSON {} -----"
     * + exe); }catch (Exception exe) { log.warn("----- ["
     * +submissionNumber+"] - {} EXCEPTION WHILE GETTING LICENSENUMBER FIELD FROM PRODUCERMATCHING RESPONSE JSON {} -----"
     * + exe); } if (StringUtils.isNotBlank(producerLicenseNumber)) {
     *
     * producerLicense = true; }
     *
     * log.info("------------ ["
     * +submissionNumber+"] - ENRICHED INDIVIDUAL LICENSE &  INDIVIDUAL LICENSE FOUND! ------------ "
     * +producerLicenseNumber+" - "+producerLicense);
     *
     * individualLicense.put(Commons.PRODUCER_LICENSE_NUMBER,
     * producerLicenseNumber); individualLicense.put(Commons.PRODUCER_LICENSE,
     * producerLicense); dataProductObject =
     * updateIndividualLicenseNumber(dataProductObject,
     * individualLicense,submissionNumber); return dataProductObject; }
     *
     * public JSONObject updateIndividualLicenseNumber(JSONObject dataProductObject,
     * JSONObject individualLicense, String submissionNumber) { JSONObject
     * updatedObj = dataProductObject; try { if(null!=updatedObj &&
     * updatedObj.has(Commons.INGESTION_DATA)&&
     * updatedObj.getJSONObject(Commons.INGESTION_DATA).has(Commons.PRODUCER)) {
     *
     * updatedObj.getJSONObject(Commons.INGESTION_DATA).getJSONObject(Commons.
     * PRODUCER) .putOpt(Commons.INDIVIDUAL_LICENSE, individualLicense); }else {
     * log.warn("["+
     * submissionNumber+"] - Producer field or standard json is not found. "); }
     *
     * } catch (JSONException e) { log.error("----- ["
     * +submissionNumber+"] - {} JSON EXCEPTION WHILE UPDATING INDIVIDUAL LICENSE DETAILS {} -----"
     * + e); }catch (Exception e) { log.error("----- ["
     * +submissionNumber+"] - {} EXCEPTION WHILE UPDATING INDIVIDUAL LICENSE DETAILS {} -----"
     * + e); } return updatedObj; }
     */}
