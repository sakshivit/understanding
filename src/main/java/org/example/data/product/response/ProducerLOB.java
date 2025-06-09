package org.example.data.product.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerLOB {
    @JsonProperty("LOB_CODE")
    private String lobCode;
    @JsonProperty("LOB_NAME")
    private String lobName;
    @JsonProperty("LOB_STAT_CODE")
    private String lobStatCode;

}
