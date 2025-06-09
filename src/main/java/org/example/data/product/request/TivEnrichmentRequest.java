package org.example.data.product.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class TivEnrichmentRequest {
    private List<Coverage> coveragesRequested;

    public TivEnrichmentRequest() {
        this.coveragesRequested = new ArrayList<>();
    }

    public void addCoverage(Coverage coverage) {
        this.coveragesRequested.add(coverage);
    }
}
