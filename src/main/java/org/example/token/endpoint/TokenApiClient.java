package org.example.token.endpoint;


import com.chubb.na.domain.token.response.TokenResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "token-api")
public interface TokenApiClient {

    @POST
    @Path("/fffcdc91-d561-4287-aebc-78d2466eec29/oauth2/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    String getToken(@HeaderParam("apiVersion") String apiVersion,
                    @FormParam("grant_type") String grantType,
                    @FormParam("client_id") String clientId,
                    @FormParam("client_secret") String clientSecret,
                    @FormParam("resource") String resource);
}
