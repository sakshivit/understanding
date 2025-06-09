package org.example.data.product.response;

import java.util.List;

import com.chubb.na.domain.data.product.dto.CustomerGroup;
import com.chubb.na.domain.data.product.dto.Policy;
import com.chubb.na.domain.data.product.dto.Product;
import com.chubb.na.domain.data.product.dto.SystemAndNormalizedProduct;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FLDerivedFieldsResponse {

	private  double revenue;
	private  String businessSegment;
	private List<CustomerGroup> customerGroup;
	private String formType;
	private String producerCode;
	private String SICCode;
	private boolean panelCheckboxFlag;
	private List<CustomerGroup> serviceArea;
	private List<SystemAndNormalizedProduct> system;
	private List<Policy> policyType;
	private List<Product> products;
	//TODO - Need to revisit this 
	private String updatedJson;
}
