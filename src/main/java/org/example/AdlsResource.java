package org.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.chubb.na.domain.kafka.producer.KafkaDomainProducer;

/**
 * This Class is added to test ADLS upload and download functionality.
 */

@Path("/adls")
public class AdlsResource {

    @Inject
    Processor processor;

    @Inject
    KafkaDomainProducer kafka;

    @GET
    @Path("/fileDownload")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        processor.validateFileDownload();
        return "File has been downloaded successfully.";
    }

    @GET
    @Path("/jsonfileDownload")
    @Produces(MediaType.TEXT_PLAIN)
    public String jsonDownload() {
        processor.validateFileDownloadJson();
        return "Json File has been downloaded successfully.";
    }

    @GET
    @Path("/fileUpload")
    @Produces(MediaType.TEXT_PLAIN)
    public String uploadFile() {
        processor.validateFileUpload();
        return "File has been uploaded successfully.";
    }
}
