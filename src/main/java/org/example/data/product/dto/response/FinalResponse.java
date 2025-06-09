package org.example.data.product.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FinalResponse<T> {
    public String id;
    public String message;
    @JsonProperty(value="is_success")
    private boolean isSuccess;
    @JsonProperty(value="return_value")
    public T returnValue;
}


