package org.example.data.product.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ODSResponse {

    @JsonProperty("configuration_key")
    private String configurationKey;
    @JsonProperty("configuration_value")
    private String configurationValue;

}
