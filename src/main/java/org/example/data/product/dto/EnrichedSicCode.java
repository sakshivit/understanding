package org.example.data.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnrichedSicCode {
    private String masterDataInsuredId;

    @JsonProperty("wipinsuredNumber")
    private String wipInsuredNumber;

    private String controlNumber;
    private String masterDataPolicyTermId;
    private String policyTypeEnglishAbbreviation;
    private String offeringStatusName;
    private String inceptionDate;
    private String expirationDate;
    private String value;
    private String srcCd;
    private String dcl;
}
