package org.example.data.product.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalNamedInsured {

	private List<String> additionalNamedInsureds = new ArrayList<>();

	public void setAdditionalNamedInsured(int index, String value) {
		while (additionalNamedInsureds.size() <= index) {
			additionalNamedInsureds.add(null);
		}
		additionalNamedInsureds.set(index, value);
	}
}
