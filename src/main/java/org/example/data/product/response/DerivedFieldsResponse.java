package org.example.data.product.response;

import com.chubb.na.domain.data.product.dto.AdditionalNamedInsured;
import com.chubb.na.domain.data.product.dto.AdditionalNamedInsuredTrimmed;
import com.chubb.na.domain.data.product.dto.TrimmedInsuredName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DerivedFieldsResponse {

	@JsonProperty("additionalNamedInsured")
	private AdditionalNamedInsured additionalNamedInsured;
	
	@JsonProperty("additionalNamedInsuredTrimmed")
	private AdditionalNamedInsuredTrimmed additionalNamedInsuredTrimmed;
	
	@JsonProperty("trimmedInsuredName")
	private TrimmedInsuredName trimmedInsuredName;
	
	@JsonProperty("expirationDate")
	private String expirationDate;
	
	@JsonProperty("lineOfBusiness")
	private String lineOfBusiness;
	
	@JsonProperty("simReceivedDate")
	private String simReceivedDate;

}