package org.example.data.product.response;

import lombok.Data;

@Data
public class CISAAssignmentResponse {
    private String branch;
    private String serviceBranch;
    private String cgCode;
    private String uwFirstName;
    private String uwLastName;
    private String uwName;
    private String uaFirstName;
    private String uaLastName;
    private String uaName;

    public CISAAssignmentResponse() {
        this.branch = "";
        this.serviceBranch = "";
        this.cgCode = "";
        this.uwFirstName = "";
        this.uwLastName = "";
        this.uwName = "";
        this.uaFirstName = "";
        this.uaLastName = "";
        this.uaName = "";
    }
}