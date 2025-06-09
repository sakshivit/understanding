package org.example.data.product.dto;

import com.chubb.na.domain.data.product.response.DerivedFieldsResponse;
import com.chubb.na.domain.data.product.response.RCDerivedFieldsResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DerivedFields {

	private DerivedFieldsResponse derivedFieldsResponse;
	private RCDerivedFieldsResponse rcDerivedFieldsResponse;
	private String flDerivedFieldsResponse;

}
