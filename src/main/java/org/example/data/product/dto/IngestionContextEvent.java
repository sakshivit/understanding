package org.example.data.product.dto;

import jakarta.json.bind.annotation.JsonbProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestionContextEvent {

	@JsonbProperty("use_case_id")
	private String useCaseId;
    private String status;
    private String stage;
    private String region;
    private String country;
    private String lob;
    private String product;
    private String message;
    private String layer;
    @JsonbProperty("number_of_submitted_documents")
    private long numberOfSubmittedDocuments;
}
