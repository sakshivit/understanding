package org.example.data.product.dto;

import com.chubb.na.domain.utils.Commons;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CisaAttributes {

    private String cisaIdentifier;
    private String complianceType;
    private String cisaExecutiveFirstName;
    private String cisaExecutiveLastName;
    private String cisaExecutiveEmail;

    public void setCisaSubmission(){
        this.cisaIdentifier = Commons.Y;
        this.complianceType = Commons.CISA_COMPLIANCE_TYPE;
    }

    public void setCisaExecutiveName(String cisaExecutiveFirstName, String cisaExecutiveLastName){
        this.cisaExecutiveFirstName = cisaExecutiveFirstName;
        this.cisaExecutiveLastName = cisaExecutiveLastName;
    }
}
