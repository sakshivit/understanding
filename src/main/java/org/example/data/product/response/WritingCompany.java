package org.example.data.product.response;

import java.util.List;

import com.chubb.na.domain.data.product.dto.Appointment;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WritingCompany {

    @JsonProperty("WRTNG_COMP_NAME")
    private String wrtngCompName;
    @JsonProperty("COMP_STAT_DESC")
    private String compStatDesc;
    @JsonProperty("COMP_STAT_CODE")
    private String compStatCode;
    @JsonProperty("APPOINTMENT")
    private List<Appointment> appointment;

}
