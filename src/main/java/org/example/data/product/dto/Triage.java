package org.example.data.product.dto;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Triage {
    String originalTriageColor;
    String triageColor;
}
