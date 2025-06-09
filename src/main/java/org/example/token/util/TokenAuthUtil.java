package org.example.token.util;

import com.chubb.na.domain.token.endpoint.TokenApiClient;
import com.chubb.na.domain.token.response.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import static org.apache.commons.lang3.StringUtils.isAllBlank;

@ApplicationScoped
public class TokenAuthUtil {
    private static Logger logger = Logger.getLogger(TokenAuthUtil.class);
    @RestClient
    TokenApiClient tokenApiClient;
    private static String GRANT_TYPE = "client_credentials";

    public TokenResponse getBearerToken(String clientId, String clientSecret, String resource, String apiVersion) {

        validateParam(clientId,clientSecret, resource);
        String response = tokenApiClient.getToken(apiVersion ,GRANT_TYPE, clientId, clientSecret, resource);

        TokenResponse token = null;
        try{
            token = new ObjectMapper().readValue(response, TokenResponse.class);
        } catch(Exception e){
            logger.info("--------------------ERROR IN GETTING TOKEN------------------------- : " + e.getMessage());
            return null;
        }
        return token;
    }

    private String validateParam(String clientId, String clientSecret, String resource)
    {
        String badParamErrorMessage = "";
        if(isAllBlank(clientId)){
            badParamErrorMessage += "[Client Id is missing]";
        }
        if(isAllBlank(clientSecret)){
            badParamErrorMessage += "[Client Secret is missing]";
        }
        if(isAllBlank(resource)){
            badParamErrorMessage += "[Resource is missing]";
        }
        if(!badParamErrorMessage.isEmpty()){
            throw new IllegalArgumentException(badParamErrorMessage);
        }
        return badParamErrorMessage;
    }
}
