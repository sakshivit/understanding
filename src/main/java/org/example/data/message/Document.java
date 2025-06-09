package org.example.data.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @JsonProperty("document_name")
    private String documentName;

    @JsonProperty("size")
    private String size;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("uri")
    private String uri;

    @Override
    public String toString() {
        return "Document{" +
                "documentName='" + documentName + '\'' +
                ", size='" + size + '\'' +
                ", stage='" + stage + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
