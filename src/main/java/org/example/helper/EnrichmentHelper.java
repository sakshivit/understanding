package org.example.helper;

import com.chubb.na.domain.utils.Commons;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jboss.logging.Logger;

import com.chubb.na.domain.data.product.request.InsuredMatchingRequest;
import com.chubb.na.domain.data.product.request.ProducerSearchRequest;
import com.chubb.na.domain.data.product.response.InsuredMatchingResponse;
import com.chubb.na.domain.data.product.response.ProducerSearchResponse;
import com.chubb.na.domain.data.product.restclient.UtilsClient;

@ApplicationScoped
public class EnrichmentHelper {

    private static final Logger logger = Logger.getLogger(EnrichmentHelper.class);

    @RestClient
    UtilsClient utilsClient;

    @Inject
    JsonHelper jsonHelper;

    public InsuredMatchingResponse getEnrichedInsured(JSONObject dataProductJsonObject) {
        JSONArray insuredObj = dataProductJsonObject.getJSONObject("ingestionData")
                .getJSONObject("submission")
                .getJSONArray("policy").getJSONObject(0).getJSONArray("insured");
        JSONArray address = insuredObj.getJSONObject(0).getJSONArray("address");
        InsuredMatchingRequest request = new InsuredMatchingRequest();
        request.setEnrichment_transaction_id(jsonHelper.getSubmissionNumberFromInputJson(dataProductJsonObject));
        request.setBusinessname(insuredObj.getJSONObject(0).optString("fullName"));
        request.setCity(address.getJSONObject(0).optString("city"));
        request.setState(address.getJSONObject(0).optString("stateOrProvinceCode"));
        request.setZipCode(address.getJSONObject(0).optString("postalCode"));

        request.setExpSic(true);

        String websiteUrl = (jsonHelper.getInsureWebsiteUrlFinalScore(dataProductJsonObject.toString()) >= Commons.WEBSITE_URL_THRESHOLD) ? insuredObj.getJSONObject(0).getJSONObject(Commons.COMMUNICATION).optString(Commons.WEBSITE_URL) : Commons.EMPTY_STRING;
        request.setWebsiteUrl(websiteUrl);
//        request.setWebsiteUrl(insuredObj.getJSONObject(0).getJSONObject("communication").optString("websiteUrl"));

        if (StringUtils.isNotBlank(address.getJSONObject(0).optString("line1"))) {
            request.setAddress1(address.getJSONObject(0).optString("line1"));
        } else {
            request.setAddress1(address.getJSONObject(0).getJSONObject("detailAddress").optString("POBox"));
        }

        request.setAddress2(address.getJSONObject(0).optString("line2"));
        logger.info("------- INSURED MATCHING REQ:- " + request.toString());
        InsuredMatchingResponse response = utilsClient.insuredMatching(request);

        return response;
    }

    public ProducerSearchResponse getEnrichedProducer(JSONObject dataProductJsonObject) {
        JSONObject producerObj = dataProductJsonObject.getJSONObject("ingestionData")
                .getJSONObject("producer");
        JSONArray addressArray = dataProductJsonObject.getJSONObject("ingestionData")
                .getJSONObject("producer").getJSONArray("address");
        ProducerSearchRequest request = new ProducerSearchRequest();

        request.setEnrichment_transaction_id(jsonHelper.getSubmissionNumberFromInputJson(dataProductJsonObject));

        String producerNumber = jsonHelper.getFinalProducerNumber(dataProductJsonObject);
        request.setExtractedProducerNumber(producerNumber);

        request.setProducerName(producerObj.optString("fullName"));
        String brokerFirstName = producerObj.getJSONObject("contact").optString("givenName");
        String brokerLastName = producerObj.getJSONObject("contact").optString("surname");
        logger.info("------BEFORE SPLIT ---------------------Broker First Name : " + brokerFirstName + ", Broker Last Name : " + brokerLastName);
        if (brokerFirstName.trim().contains(" ")){
            logger.info("------SPLITTING BROKER NAME ---------------------");
            String[] name = brokerFirstName.split(" ");
            brokerFirstName = name[0];
            brokerLastName = name[1];
            logger.info("------AFTER SPLIT ---------------------Broker First Name : " + brokerFirstName + ", Broker Last Name : " + brokerLastName);
        }
        request.setAgentFirstName(brokerFirstName);
        request.setAgentLastName(brokerLastName);
        request.setLicenseState(dataProductJsonObject.getJSONObject("ingestionData")
                .getJSONObject("submission").getJSONArray("policy")
                .getJSONObject(0).getJSONArray("insured").getJSONObject(0)
                .getJSONArray("address").getJSONObject(0)
                .optString("stateOrProvinceCode"));

        if (addressArray.length() > 1){
            for (int i=0; i<addressArray.length(); i++) {
                JSONObject addressObj = addressArray.getJSONObject(i);
                if(addressObj.optString("typeCode").equalsIgnoreCase("Agency")){
                    request.setCity(addressObj.optString("city"));
                    request.setState(addressObj.optString("stateOrProvinceCode"));
                    request.setAddress1(addressObj.optString("line1"));
                    request.setAddress2(addressObj.optString("line2"));
                    request.setZipCode(addressObj.optString("postalCode"));

                } else {
                    continue;
                }
            }
        } else {
            JSONObject addressObj = addressArray.getJSONObject(0);
            request.setCity(addressObj.optString("city"));
            request.setState(addressObj.optString("stateOrProvinceCode"));
            request.setAddress1(addressObj.optString("line1"));
            request.setAddress2(addressObj.optString("line2"));
            request.setZipCode(addressObj.optString("postalCode"));
        }

        // D8INGEST-6454 D8INGEST-6455
        String cisaIdentifier = jsonHelper.getCISAIdentifier(dataProductJsonObject.toString());
        if(cisaIdentifier.equalsIgnoreCase(Commons.Y)){
            String submissionNUmber = jsonHelper.getSubmissionNumberFromInputJson(dataProductJsonObject);
            logger.info("------------SUBMISSION NO: "+submissionNUmber +" CALLING CISA PRODUCER ENRICHMENT API-----------------");
            request.setBusinessCategoryCodeList(Commons.CISA_BUSINESS_CATEGORY_CODE_LIST);
            String extendedProdCode = "";
            String producerCode = jsonHelper.getExtractedProducerChubbCode(dataProductJsonObject);
            String prodSubCode = jsonHelper.getExtractedProducerChubbSubCode(dataProductJsonObject);
            extendedProdCode = producerCode+"-"+prodSubCode;
            request.setExtractedProducerNumber(extendedProdCode);
        }
        logger.info("------- PRODUCER MATCHING REQ:- " + request.toString());
        ProducerSearchResponse response = utilsClient.producerMatching(request);
        return response;

    }
}
