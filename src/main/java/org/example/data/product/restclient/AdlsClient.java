package org.example.data.product.restclient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

import com.chubb.na.domain.data.product.dto.MultipartBody;

@RegisterRestClient
public interface AdlsClient {

	@GET
	@Path("/chubb-docai-dp-data-orchestration/file/download")
	public String downloadFile(@QueryParam("uri") String param);

	@POST
	@Path("/chubb-docai-dp-data-orchestration/file/upload/batch")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String uploadFile(@MultipartForm MultipartBody data);

}
