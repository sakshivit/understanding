package org.example.data.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngestionContext {
    @JsonProperty("use_case_name")
    private String useCaseName;

    @JsonProperty("use_case_id")
    private String useCaseId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("region")
    private String region;

    @JsonProperty("country")
    private String country;

    @JsonProperty("lob")
    private String lob;

    @JsonProperty("product")
    private String product;

    @JsonProperty("event_level")
    private String eventLevel;

    @JsonProperty("vendor")
    private String vendor;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("message")
    private String message;

    @JsonProperty("documents")
    private List<Document> documents;

    @JsonProperty("number_of_submitted_documents")
    private int numberOfSubmittedDocuments;

    @JsonProperty("layer")
    private String layer;

    @JsonProperty("policies")
    private List<Object> policies; // Assuming policies is an array of objects

    @Override
    public String toString() {
        return "IngestionContext{" +
                "useCaseName='" + useCaseName + '\'' +
                ", useCaseId='" + useCaseId + '\'' +
                ", status='" + status + '\'' +
                ", stage='" + stage + '\'' +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                ", lob='" + lob + '\'' +
                ", product='" + product + '\'' +
                ", eventLevel='" + eventLevel + '\'' +
                ", vendor='" + vendor + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", message='" + message + '\'' +
                ", documents=" + documents +
                ", numberOfSubmittedDocuments=" + numberOfSubmittedDocuments +
                ", layer='" + layer + '\'' +
                ", policies=" + policies +
                '}';
    }
}
