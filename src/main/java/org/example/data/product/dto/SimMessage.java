package org.example.data.product.dto;

import lombok.Data;

@Data
public class SimMessage {

	private String submission_number;
	private String source_reference_number;
	private String event_type;
	private String event_description;
	private String queue_name;
}
