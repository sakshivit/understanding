package org.example.data.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MdmDetails {
	String sicCode;
	String insuredName;
	String wipInsuredNumber;
	List<String>  wipInsuredNumbers;
	String masterDataInsuredId;
	String matchScore;
	
}
