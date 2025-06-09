package org.example.sim.beans;

import com.chubb.na.domain.data.product.dto.response.FinalResponse;
import com.chubb.na.domain.sim.beans.SimIncidentDetails;
import com.chubb.na.domain.sim.beans.UpdateIncidentResponse;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;


@RegisterRestClient
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SimUpdateClient {

    @PATCH
    @Path("/incident/{incidentId}")
    public FinalResponse<UpdateIncidentResponse> updateIncident(@PathParam("incidentId") int incidentId, SimIncidentDetails incident);

}