package org.example.sim;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import com.chubb.na.domain.data.product.dto.response.FinalResponse;
import com.chubb.na.domain.sim.beans.SimIncidentDetails;
import com.chubb.na.domain.sim.beans.SimLookUpNameInput;
import com.chubb.na.domain.sim.beans.UpdateIncidentResponse;
import com.chubb.na.domain.sim.endpoint.SimBaseClient;
import com.chubb.na.domain.sim.endpoint.SimUpdateClient;
import com.chubb.na.domain.token.util.TokenAuthUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SimService {
    private static final Logger logger = Logger.getLogger(SimService.class);

    @RestClient
    SimBaseClient client;
    @RestClient
    SimUpdateClient updateClient;
    @Inject
    TokenAuthUtil tokenAuthUtil;
    @ConfigProperty(name = "sim.api.version")
    String apiVersion;
    @ConfigProperty(name = "sim.subscription.key")
    String subscriptionKey;
    @ConfigProperty(name = "sim.app.id")
    String appID;
    @ConfigProperty(name = "sim.app.key")
    String appKey;
    @ConfigProperty(name = "sim.resource.id")
    String resourceID;
    @ConfigProperty(name = "auth.version")
    String authVersion;



    public boolean updateIncidentDetails(Long incidentId, String product, String transaction, String segment) {
        logger.info("-----------------UPDATING SIM PRODUCT, TRANSACTION, MARKET SEGMENT-----------------" + product + ", " + transaction + ", " + segment);
        SimIncidentDetails incidentTo = new SimIncidentDetails();
        SimLookUpNameInput simProduct = new SimLookUpNameInput();
        simProduct.setLookupName(product);
        SimLookUpNameInput simTransaction = new SimLookUpNameInput();
        simTransaction.setLookupName(transaction);
        SimLookUpNameInput marketSegment = new SimLookUpNameInput();
        marketSegment.setLookupName(segment);

        SimIncidentDetails.CustomFields.C customFields = new SimIncidentDetails.CustomFields.C();
        customFields.setSegment(marketSegment);
        customFields.setTransaction(simTransaction);
        SimIncidentDetails.CustomFields customField = new SimIncidentDetails.CustomFields();
        customField.setC(customFields);

        incidentTo.setProduct(simProduct);
        incidentTo.setCustomFields(customField);
        logger.info("----------------UPDATING FIELDS PRODUCT, TRANSACTION AND SEGMENT FOR SIM INCIDENT-------------------" + incidentId + ", incident Details : " + incidentTo);
        return updateIncident(incidentId, incidentTo);
    }

    public boolean updateIncidentQueue(Long incidentId, String queueName) {
        try {
            SimIncidentDetails incidentTo = new SimIncidentDetails();
            SimLookUpNameInput lookUpNameInput = new SimLookUpNameInput();
            SimIncidentDetails.SimAssignedTo assigned = new SimIncidentDetails.SimAssignedTo();
            lookUpNameInput.setLookupName(queueName);
            incidentTo.setQueue(lookUpNameInput);
            incidentTo.setAssignedTo(assigned);
            return updateIncident(incidentId, incidentTo);
        }catch (Exception e) {
            logger.warn("-------EXCEPTION WHILE  updateIncidentQueue--------" + incidentId + "-----" + e.getMessage());
            return false;
        }

    }

    public boolean updateIncidentStatus(Long incidentId, String status) {
        try {
            SimIncidentDetails incidentTo = new SimIncidentDetails();

            SimLookUpNameInput lookUpNameInput = new SimLookUpNameInput();
            lookUpNameInput.setLookupName(status);

            SimIncidentDetails.StatusWithType statusWithType = new SimIncidentDetails.StatusWithType();
            statusWithType.setStatus(lookUpNameInput);

            incidentTo.setStatusWithType(statusWithType);

            return updateIncident(incidentId, incidentTo);
        } catch (Exception e) {
            logger.warn(
                    "-------EXCEPTION WHILE UPDATING INCIDENT STATUS--------" + incidentId + "-----" + e.getMessage());
            return false;
        }

    }

    public boolean updateIncident(Long incidentId, SimIncidentDetails incidentTo) {
        FinalResponse<UpdateIncidentResponse> updateResponse = updateClient.updateIncident(Integer.parseInt(Long.toString(incidentId)), incidentTo);
        if (updateResponse.isSuccess()){
            return true;
        }
        return false;
    }


    public SimIncidentDetails getIncidentDetails(Long incidentId) {
        logger.info("----- SIM ID: " + incidentId + " -----");
        SimIncidentDetails response = new SimIncidentDetails();
        try {
            response = client.getIncident(incidentId, getAuthToken(), apiVersion, subscriptionKey);
            logger.info("----- SUCCESS IN GETTING SIM DETAILS -----");
        } catch (Exception e) {
            logger.info("----- ERROR WHILE GETTING SIM INCIDENT DETAILS -----" + e);
        }
        return response;
    }

    private String getAuthToken() {
        String token = tokenAuthUtil.getBearerToken(appID, appKey, resourceID, authVersion).getAccessToken();
        return "Bearer " + token;
    }
}