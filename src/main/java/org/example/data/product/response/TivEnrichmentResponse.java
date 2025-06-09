package org.example.data.product.response;

import com.chubb.na.domain.data.product.request.Coverage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TivEnrichmentResponse {
    private List<Coverage> coveragesRequested;
    private String tiv;
}
