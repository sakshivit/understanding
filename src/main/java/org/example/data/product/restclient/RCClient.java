package org.example.data.product.restclient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface RCClient {

	@GET
	@Path("/enrich/rcfields")
	public String enrich(String request);

	@GET
	@Path("/enrich/clearanceCheck")
	public String performClearanceCheck(String request);
	
}
