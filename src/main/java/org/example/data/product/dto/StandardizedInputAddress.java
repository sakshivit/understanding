package org.example.data.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardizedInputAddress {
	private String address1;
    private String address2;
    private String city;
    private String state;
    private String zipCode;
    private String zip4;
    private String zip5;
    @JsonProperty("dcl_standardized_input_address")
    private String dclStandardizedInputAddress;

}
