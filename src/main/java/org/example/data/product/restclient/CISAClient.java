package org.example.data.product.restclient;

import com.chubb.na.domain.data.product.response.CISAAssignmentResponse;
import com.chubb.na.domain.data.product.response.CISAExecutiveResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface CISAClient {

    @GET
    @Path("/ipa-naops.ipa.ingestion-uw-assignment/ingestion/cisa/attributes")
    CISAAssignmentResponse getCISA(
            @HeaderParam("Authorization") String token,
            @HeaderParam("apiVersion") String apiVersion,
            @HeaderParam("Ocp-Apim-Subscription-Key") String subscriptionKey,
            @QueryParam("branch") String branch,
            @QueryParam("cg") String cg,
            @QueryParam("agencyName") String agencyName,
            @QueryParam("policyType") String policyType
    );

    @GET
    @Path("/ipa-naops.ipa.ingestion-uw-assignment/ingestion/cisa/execuitve")
    CISAExecutiveResponse getCISAExecutive(
            @HeaderParam("Authorization") String token,
            @HeaderParam("apiVersion") String apiVersion,
            @HeaderParam("Ocp-Apim-Subscription-Key") String subscriptionKey,
            @QueryParam("branch") String branch
    );

}
