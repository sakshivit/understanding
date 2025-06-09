package org.example.data.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceContext {
    @JsonProperty("submission_number")
    private String submissionNumber;

    @JsonProperty("rush_indicator")
    private boolean rushIndicator;

    @JsonProperty("source_system_id")
    private String sourceSystemId;

    @JsonProperty("source_system_name")
    private String sourceSystemName;

    @JsonProperty("source_reference_number")
    private String sourceReferenceNumber;

    @JsonProperty("source_reference_json")
    private String sourceReferenceJson;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("queue_name")
    private String queueName;

    @JsonProperty("email_subject")
    private String emailSubject;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("extraction_id")
    private String extractionId;

    @Override
    public String toString() {
        return "SourceContext{" +
                "submissionNumber='" + submissionNumber + '\'' +
                ", rushIndicator=" + rushIndicator +
                ", sourceSystemId='" + sourceSystemId + '\'' +
                ", sourceSystemName='" + sourceSystemName + '\'' +
                ", sourceReferenceNumber='" + sourceReferenceNumber + '\'' +
                ", sourceReferenceJson='" + sourceReferenceJson + '\'' +
                ", channel='" + channel + '\'' +
                ", queueName='" + queueName + '\'' +
                ", emailSubject='" + emailSubject + '\'' +
                ", priority='" + priority + '\'' +
                ", extractionId='" + extractionId + '\'' +
                '}';
    }
}
