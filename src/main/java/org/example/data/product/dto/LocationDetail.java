package org.example.data.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetail{
    private Address1 address1;
    private Address2 address2;
    private City city;
    private State state;
    private Zip zip;
    @JsonProperty("chubb_location_id")
    private ChubbLocationId chubbLocationId;
}
