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
public class SourceContextEvent {

	@JsonbProperty("submission_number")
    private String submissionNumber;
	@JsonbProperty("source_system_id")
    private String sourceSystemID;
	@JsonbProperty("source_reference_number")
    private String sourceReferenceNumber;
    @JsonbProperty("source_reference_json")
    private String sourceReferenceJson;
    @JsonbProperty("priority")
    private String priority;
    private String channel;

}
