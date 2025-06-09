package org.example.data.product.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuredMatchingRequest {

	private String enrichment_transaction_id;
	private String businessname;
	private String address1;
	private String address2;
	private String city;
	private String state;
	private String zipCode;
	private String websiteUrl;
	private boolean expSic;
}
