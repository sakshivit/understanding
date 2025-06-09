package org.example.data.product.restclient;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@RegisterRestClient
public interface CibClient {
    @GET
    @Path("/enrich/cibDerivedFields")
    public String enrich(String request);

    @GET
    @Path("/enrich/clearanceCheck")
    public String performClearanceCheck(String request);
}
