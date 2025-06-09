package org.example.data.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalNamedInsuredTrimmed {

	private List<String> additionalNamedInsuredTrimmed = new ArrayList<>();

	public void setAdditionalNamedInsuredTrimmed(int index, String value) {
		while (additionalNamedInsuredTrimmed.size() <= index) {
			additionalNamedInsuredTrimmed.add(null);
		}
		additionalNamedInsuredTrimmed.set(index, value);
	}
}
