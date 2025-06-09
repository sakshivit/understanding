package org.example.data.product.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuredMatchingOutput {

    private BusinessDetails businessDetails;
    private List<LocationDetail> locationDetails;
    private List<CosmosDocument> cosmosDocuments;
    private MdmDetails mdmDetails;
    private int dnbConfidenceCode;
    private String matchType;
    private int nameMatchRatio;
    private String id;
    @JsonProperty("standardized_input_address")
    private StandardizedInputAddress standardizedInputAddress;

    @JsonProperty("enriched_sic_code")
    private List<EnrichedSicCode> enrichedSicCode;

}
