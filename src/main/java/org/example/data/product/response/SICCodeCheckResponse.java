package org.example.data.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SICCodeCheckResponse {

	private String sicCode;
	private String result;
	
}
