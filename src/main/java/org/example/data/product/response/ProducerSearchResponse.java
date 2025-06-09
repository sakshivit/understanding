package org.example.data.product.response;

import com.chubb.na.domain.data.product.request.ProducerSearchRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProducerSearchResponse {

    private ProducerSearchRequest originalRequest;
    private List<ProducerSearchOutput> output;
}
