
package org.example.data.product.dto;

import jakarta.json.bind.annotation.JsonbProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceContext {

	@JsonbProperty("submission_number")
    private String submissionNumber;
	@JsonbProperty("source_system_id")
    private String sourceSystemId;
	@JsonbProperty("source_system_name")
    private String sourceSystemName;
	@JsonbProperty("source_reference_number")
    private String sourceReferenceNumber;
    private String channel;

}
