package org.example.data.product.dto;
 
 
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
 
    @JsonProperty("APMNT_EFF_DATE")
    private String apmntEffDate;
 
    @JsonProperty("APMNT_EXP_DATE")
    private String apmntExpDate;
 
    @JsonProperty("PAS_APMNT_STAT_CODE")
    private String pasApmntStatCode;
 
    @JsonProperty("PAS_APMNT_STAT_CODE_DESC")
    private String pasApmntStatCodeDesc;
 
    @JsonProperty("ST_LIC_NUM")
    private String stLicNum;
 
    @JsonProperty("LIC_ST_NAME")
    private String licStName;
 
    @JsonProperty("LIC_ST_CODE")
    private String licStCode;
 
    @JsonProperty("PAS_LIC_TYPE")
    private String pasLicType;
 
    @JsonProperty("PAS_LIC_TYPE_CODE")
    private String pasLicTypeCode;
 
    @JsonProperty("LICEE_PMRY_FRST_NAME")
    private String liceePmryFrstName;
 
    @JsonProperty("LICEE_PMRY_LAST_NAME")
    private String liceePmryLastName;
 
    @JsonProperty("LICEE_PMRY_MID_NAME")
    private String liceePmryMidName;
 
    @JsonProperty("LICEE_ENTY_FULL_NAME")
    private String liceeEntyFullName;
 
    @JsonProperty("LICEE_TYPE_CODE")
    private String liceeTypeCode;
 
    @JsonProperty("LICEE_TYPE_DESC")
    private String liceeTypeDesc;
 
    @JsonProperty("WRTNG_COMP_CODE")
    private String wrtingCompCode;
 
    @JsonProperty("WRTNG_COMP_NAME")
    private String wrtingCompName;
 
}