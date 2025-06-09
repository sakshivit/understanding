package org.example.data.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDetails{
	@JsonProperty("business_name")
    private BusinessName businessName;
	@JsonProperty("ownership")
    private Ownership ownership;
	@JsonProperty("business_segment")
    private String businessSegment;
	@JsonProperty("alternate_name1")
    private AlternateName1 alternateName1;
	@JsonProperty("alternate_name2")
    private AlternateName2 alternateName2;
	@JsonProperty("alternate_name3")
    private AlternateName3 alternateName3;
	@JsonProperty("alternate_name4")
    private AlternateName4 alternateName4;
	@JsonProperty("alternate_name5")
    private AlternateName5 alternateName5;
	@JsonProperty("naics_code")
    private NaicsCode naicsCode;
	@JsonProperty("primary_sic_code")
    private PrimarySicCode primarySicCode;
	@JsonProperty("revenue_amount")
    private RevenueAmount revenueAmount;
	@JsonProperty("sic_code1")
    private SicCode1 sicCode1;
	@JsonProperty("sic_code2")
    private SicCode2 siCode2;
	@JsonProperty("sic_code3")
    private SicCode3 sicCode3;
    private Fein fein;
    private DunsNo duns_no;
    @JsonProperty("dcl_business_segment")
    private String dclBusinessSegment;
    @JsonProperty("company_age")
    private CompanyAge companyAge;
    @JsonProperty("climate_sector")
    private Climate climateSector;
    @JsonProperty("climate_asset")
    private Climate climateAsset;
    
}