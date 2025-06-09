package org.example.data.product.dto;

import jakarta.json.bind.annotation.JsonbProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaIntakeMessage {
	
	@JsonbProperty("source_context")
    private SourceContext sourceContext;
	@JsonbProperty("ingestion_context")
    private IngestionContext ingestionContext;
    private String sla;

}
