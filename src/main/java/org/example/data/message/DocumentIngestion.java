package org.example.data.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIngestion {

    @JsonProperty("ingestion_context")
    private IngestionContext ingestionContext;

    @JsonProperty("source_context")
    private SourceContext sourceContext;

    @Override
    public String toString() {
        return "DocumentIngestion{" +
                "ingestionContext=" + ingestionContext +
                ", sourceContext=" + sourceContext +
                '}';
    }
}

