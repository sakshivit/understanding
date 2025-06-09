package org.example.data.product.request;

import java.util.List;

import com.chubb.na.domain.utils.Commons;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerSearchRequest {

	private String enrichment_transaction_id;

	@JsonProperty("ExtractedProducerNumber")
	private String extractedProducerNumber;

	@JsonProperty("ProducerName")
	private String producerName;

	@JsonProperty("Address1")
	private String address1;

	@JsonProperty("Address2")
	private String address2;

	@JsonProperty("City")
	private String city;

	@JsonProperty("State")
	private String state;

	@JsonProperty("ZipCode")
	private String zipCode;

	@JsonProperty("AgentFirstName")
	private String agentFirstName;

	@JsonProperty("AgentLastName")
	private String agentLastName;

	@JsonProperty("LicenseState")
	private String licenseState;

	//Last 4 fields are hardcoded for Kafka
	@JsonProperty("BusinessCategoryCodeList")
	private List<String> businessCategoryCodeList = Commons.BUSINESS_CATEGORY_CODE_LIST;

	@JsonProperty("ProducerContextCode")
	private String producerContextCode = Commons.COM;

	@JsonProperty("PasProducerStatusCode")
	private String pasProducerStatusCode = Commons.OPN;

	@JsonProperty("BranchExclusionList")
	private List<String> branchExclusionList = Commons.BRANCH_EXCLUSION_LIST;

}
