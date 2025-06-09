
package org.example.data.product.dto;

import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionContext {

	@JsonbProperty("use_case_name")
	private String useCaseName;
	@JsonbProperty("use_case_id")
	private String useCaseId;
	private String status;
	private String stage;
	@JsonbProperty("event_id")
	private String eventId;
	@JsonbProperty("event_type")
	private String eventType;
	private String region;
	private String country;
	@JsonbProperty("lob")
	private String lob;
	private String product;
	@JsonbProperty("user_name")
	private String userName;
	@JsonbProperty("event_level")
	private String eventLevel;
	private String vendor;
	private String message;
	@JsonbProperty("start_datetime")
	private String startDatetime;
	@JsonbProperty("end_datetime")
	private String endDatetime;
	@JsonbProperty("event_timestamp")
	private String eventTimestamp;
	private List<Document> documents;
	@JsonbProperty("number_of_submitted_documents")
	private long numberOfSubmittedDocuments;
	private String layer;
}
