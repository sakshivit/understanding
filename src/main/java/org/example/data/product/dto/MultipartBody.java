package org.example.data.product.dto;

import org.jboss.resteasy.reactive.PartType;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;


public class MultipartBody {

    @FormParam("files")
    @PartType(MediaType.MULTIPART_FORM_DATA)
    public byte[] files;

    @FormParam("event_schema")
    @PartType(MediaType.TEXT_PLAIN)
    public String eventSchema;
}
