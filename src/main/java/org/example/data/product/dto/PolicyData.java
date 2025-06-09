package org.example.data.product.dto;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyData {
    String policyType;
    String coverageStatus;
    String declineReason;
    String customerGroup;
    String departmentCode;
    String uwFirstName;
    String uwLastName;
    String uaFirstName;
    String uaLastName;
    String uwDefaultName;
    String uaDefaultName;
    String serviceBranch;
    String rateIndicator;
    String marketingProgramCode;
    String serviceArea;
}
