package org.example.data.product.dto;

import lombok.Data;

@Data
public class ClimateTech {

	private String indicator;
	private ClimateTechDetails climateSector;
	private ClimateTechDetails climateAsset;
	private String climateDataSource;
}
