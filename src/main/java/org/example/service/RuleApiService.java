package org.example.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chubb.na.domain.data.product.request.ClimateRequest;
import com.chubb.na.domain.data.product.request.StateAbbreviationRequest;
import com.chubb.na.domain.data.product.request.TivEnrichmentRequest;
import com.chubb.na.domain.data.product.response.ClimateResponse;
import com.chubb.na.domain.data.product.response.StateAbbreviationResponse;
import com.chubb.na.domain.data.product.response.TivEnrichmentResponse;
import com.chubb.na.domain.data.product.restclient.RulesApiClient;
import com.chubb.na.domain.token.util.TokenAuthUtil;
import com.chubb.na.domain.utils.Commons;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RuleApiService {

    private static Logger logger = LoggerFactory.getLogger(RuleApiService.class);
    private Gson gson = new GsonBuilder().create();

    @RestClient
    RulesApiClient rulesApiClient;
    @Inject
    TokenAuthUtil tokenAuthUtil;
    @ConfigProperty(name = "rules.api.version")
    String apiVersion;
    @ConfigProperty(name = "rules.subscription.key")
    String subscriptionKey;
    @ConfigProperty(name = "rules.app.id")
    String appID;
    @ConfigProperty(name = "rules.app.key")
    String appKey;
    @ConfigProperty(name = "rules.resource.id")
    String resourceID;
    @ConfigProperty(name = "auth.version")
    String authVersion;

    public TivEnrichmentResponse tivEnrichmentRuleService(TivEnrichmentRequest request) {
        TivEnrichmentResponse response = new TivEnrichmentResponse();
        response.setTiv("0.00");

        try {
            logger.info("----- RULES ENGINE TIV ENRICHMENT REQUEST: " + gson.toJson(request) + " -----");
            return rulesApiClient.enrichTiv(getAuthToken(), apiVersion,
                    subscriptionKey, gson.toJson(request));
        } catch (Exception ex) {
            logger.error("----- ERROR WHILE CALLING RULES ENGINE API FOR TIV ENRICHMENT: " + ex);
        }

        return response;
    }

    public StateAbbreviationResponse getStateAbbreviationService(StateAbbreviationRequest request) {
        StateAbbreviationResponse response = new StateAbbreviationResponse();

        try {
            logger.info("----- RULES SATE ABBREVIATION REQUEST: " + gson.toJson(request) + " -----");
            response = rulesApiClient.getStateAbbreviation(getAuthToken(), apiVersion,
                    subscriptionKey, gson.toJson(request));
        } catch (Exception e) {
            logger.error("----- ERROR WHILE CALLING RULES ENGINE API FOR STATE ABBREVIATION: " + e);
            response.setMappedAbbreviation(Commons.EMPTY_STRING);
        }

        return response;
    }

    public ClimateResponse getClimateDescription(ClimateRequest request) {
        ClimateResponse response = new ClimateResponse();

        try {
            logger.info("----- RULES CLIMATE REQUEST: " + gson.toJson(request) + " -----");
            response = rulesApiClient.getClimateDescription(getAuthToken(), apiVersion,
                    subscriptionKey, gson.toJson(request));
        } catch (Exception ex) {
            logger.error("----- ERROR WHILE CALLING RULES ENGINE API FOR CLIMATE: " + ex);
            response.setDescription(Commons.EMPTY_STRING);
        }

        return response;
    }

    private String getAuthToken() {
        String token = tokenAuthUtil.getBearerToken(appID, appKey, resourceID, authVersion).getAccessToken();
        return "Bearer " + token;
    }

}

