package org.example.data.product.restclient;

import com.chubb.na.domain.data.product.response.ODSResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface ODSClient {

    @GET
    @Path("/configuration/options/by-key/{key}")
    public ODSResponse getConfiguration(@PathParam("key") String key);

}
