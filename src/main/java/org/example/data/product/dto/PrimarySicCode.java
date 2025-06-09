package org.example.data.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrimarySicCode {
    private String value;
    private String confidence;
    private String dcl;

}
