package org.example.data.product.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.chubb.na.domain.data.product.dto.PolicyData;
import com.chubb.na.domain.data.product.dto.Triage;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RCDerivedFieldsResponse {

	private String sicCode;
	private String producerNumber;
	private double tiv;
	private int noOfVehicles;
	private int noOfLocations;
	private List<PolicyData> policyData;
	private Triage triage;
	private String middleMarket;
	private String product;

	public void setPolicyTypes(List<PolicyData> policyData) {
		this.policyData = policyData;
	}
}