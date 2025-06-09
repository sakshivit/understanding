package org.example.data.product.response;

import lombok.Data;

@Data
public class CISAExecutiveResponse {

    private String cisaExecutiveFirstName;
    private String cisaExecutiveLastName;
    private String cisaExecutiveName;

    public CISAExecutiveResponse() {

        this.cisaExecutiveFirstName = "";
        this.cisaExecutiveLastName = "";
        this.cisaExecutiveName = "";

    }

}