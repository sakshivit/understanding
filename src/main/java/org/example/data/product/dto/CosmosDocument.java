package org.example.data.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CosmosDocument{
    private Website website;
    @JsonProperty("dnb_employee_count")
    private DnbEmployeeCount dnbEmployeeCount;
    @JsonProperty("da_no_of_business_locations")
    private String daNoOfBusinessLocations;
}
