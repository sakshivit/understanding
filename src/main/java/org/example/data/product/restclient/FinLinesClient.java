package org.example.data.product.restclient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.chubb.na.domain.data.product.dto.response.FinalResponse;
import com.fasterxml.jackson.databind.JsonNode;

@RegisterRestClient
public interface FinLinesClient {

	@GET
	@Path("/derive/derivefinlines")
	public String enrich(String request);

	@POST
	@Path("/derive/clearanceAndSubmission")
	public FinalResponse<JsonNode> clearanceAndSubmission(String request, @QueryParam("queueName") String queue);

	@GET
	@Path("/derive/clearanceCheck")
	public String performClearanceCheck(String request);
	
}
