package org.example.data.product.restclient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.chubb.na.domain.data.product.request.InsuredMatchingRequest;
import com.chubb.na.domain.data.product.request.ProducerSearchRequest;
import com.chubb.na.domain.data.product.response.InsuredMatchingResponse;
import com.chubb.na.domain.data.product.response.ProducerSearchResponse;

import feign.Response;

@RegisterRestClient
public interface UtilsClient {

	@GET
	@Path("/enrich/derivedFields")
	public String enrich(String request);
	
	@GET
	@Path("/enrich/insuredMatching")
	public InsuredMatchingResponse insuredMatching(InsuredMatchingRequest request);

	@GET
	@Path("/enrich/producerMatching")
	public ProducerSearchResponse producerMatching(ProducerSearchRequest request);
	
	@POST
	@Path("/submitToClearance/retailCommercial")
	public String submitRCToClearance(String request);
	
	@POST
	@Path("/submitToClearance/cib")
	public String submitCibToClearance(String request);

	@POST
	@Path("/enrich/brokerFields")
	@Produces("application/json")
	@Consumes("application/json")
	public String enrichBrokerFields(String request);
	
}
