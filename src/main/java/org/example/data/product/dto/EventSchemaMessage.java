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
public class EventSchemaMessage {

	@JsonbProperty("source_context")
    private SourceContextEvent sourceContext;
	@JsonbProperty("ingestion_context")
    private IngestionContextEvent ingestionContext;

}
