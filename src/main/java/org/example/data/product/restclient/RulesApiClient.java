package org.example.data.product.restclient;

import com.chubb.na.domain.data.product.response.StateAbbreviationResponse;
import com.chubb.na.domain.data.product.response.TivEnrichmentResponse;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.chubb.na.domain.data.product.response.ClimateResponse;
import com.chubb.na.domain.data.product.response.SICCodeCheckResponse;

@RegisterRestClient
public interface RulesApiClient {

	@GET
	@Path("/ipa-naops.ipa.ingestion-rules/ingestion/rules/siccheck")
	SICCodeCheckResponse getSICCheckResult(
			@HeaderParam("Authorization") String token,
			@HeaderParam("apiVersion") String apiVersion,
			@HeaderParam("Ocp-Apim-Subscription-Key") String subscriptionKey,
			@QueryParam("sicCode") String sicCode
	);

	@POST
	@Path("/ipa-naops.ipa.ingestion-rules/ingestion/rules/enrichTiv")
	TivEnrichmentResponse enrichTiv(
			@HeaderParam("Authorization") String token,
			@HeaderParam("apiVersion") String apiVersion,
			@HeaderParam("Ocp-Apim-Subscription-Key") String subscriptionKey,
			String body
	);

	@POST
	@Path("/ipa-naops.ipa.ingestion-rules/ingestion/rules/stateAbbreviations")
	StateAbbreviationResponse getStateAbbreviation(
			@HeaderParam("Authorization") String token,
			@HeaderParam("apiVersion") String apiVersion,
			@HeaderParam("Ocp-Apim-Subscription-Key") String subscriptionKey,
			String body
	);
	
	@POST
	@Path("/ipa-naops.ipa.ingestion-rules/ingestion/rules/climate")
	ClimateResponse getClimateDescription(
			@HeaderParam("Authorization") String token,
			@HeaderParam("apiVersion") String apiVersion,
			@HeaderParam("Ocp-Apim-Subscription-Key") String subscriptionKey,
			String body
	);
}
