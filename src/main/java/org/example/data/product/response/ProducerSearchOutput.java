package org.example.data.product.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerSearchOutput {

    @JsonProperty("disttype")
    private String disttype;
    @JsonProperty("BUSN_PARTY_GUID")
    private String busnPartyGuid;
    @JsonProperty("PROD_CNTRCTD_NAME")
    private String prodCntrctdName;
    @JsonProperty("FRMR_PROD_NAME")
    private Object frmrProdName;
    @JsonProperty("PAS_PROD_CODE")
    private String pasProdCode;
    @JsonProperty("PROD_NATNL_CODE")
    private String prodNatnlCode;
    @JsonProperty("PROD_MSTR_CODE")
    private String prodMstrCode;
    @JsonProperty("PROD_NUM")
    private Object prodNum;
    @JsonProperty("SUB_PROD_NUM")
    private Object subProdNum;
    @JsonProperty("PROD_CNTXT_CODE")
    private String prodCntxtCode;
    @JsonProperty("BUSN_CTGY_CODE")
    private String busnCtgyCode;
    @JsonProperty("BUSN_CTGY_NAME")
    private String busnCtgyName;
    @JsonProperty("PAS_PROD_STAT_CODE")
    private String pasProdStatCode;
    @JsonProperty("PAS_PROD_STAT_DESC")
    private String pasProdStatDesc;
    @JsonProperty("BRNCH_CODE")
    private Object brnchCode;
    @JsonProperty("BRNCH_NAME")
    private Object brnchName;
    @JsonProperty("BUSN_ADDR1_LINE_TXT")
    private String busnAddr1LineTxt;
    @JsonProperty("BUSN_ADDR2_LINE_TXT")
    private String busnAddr2LineTxt;
    @JsonProperty("BUSN_CITY_NAME")
    private String busnCityName;
    @JsonProperty("BUSN_ST_CODE")
    private String busnStCode;
    @JsonProperty("BUSN_ZIP_CODE")
    private String busnZipCode;
    @JsonProperty("PRODUCER_LOB")
    private List<ProducerLOB> producerLob;
    @JsonProperty("WRITING_COMPANY")
    private List<WritingCompany> writingCompany;
    @JsonProperty("dcl_producer_match")
    private String dclProducerMatch;
    @JsonProperty("Producer_License#")
    private String producerLicenseNumber;
}
