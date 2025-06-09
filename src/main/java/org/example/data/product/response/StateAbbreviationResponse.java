package org.example.data.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StateAbbreviationResponse {
    private String requestedState;
    private String mappedAbbreviation;
}
