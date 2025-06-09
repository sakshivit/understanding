package org.example.data.product.dto;
import jakarta.json.bind.annotation.JsonbProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

	@JsonbProperty("document_name")
	private String documentName;
	@JsonbProperty("document_type")
	private String documentType;
	private String size;
	private String stage;
	@JsonbProperty("mime_type")
	private String mimeType;
	@JsonbProperty("classification_type")
	private String classificationType;
	private String uri;
	private String content;
}
