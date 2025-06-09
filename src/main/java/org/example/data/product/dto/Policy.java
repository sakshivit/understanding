package org.example.data.product.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Policy {

	private String normalizedProduct;
	private String system;
	private String policyType;
	
}
