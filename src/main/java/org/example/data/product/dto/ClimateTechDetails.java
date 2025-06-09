package org.example.data.product.dto;

import org.apache.commons.lang3.StringUtils;
import lombok.Data;

@Data
public class ClimateTechDetails {

	private String value;
	private String description;

	public void setValueOrDefault(String value, String dflt) {
		this.setValue(StringUtils.isBlank(value) ? dflt : value);
	}

}
