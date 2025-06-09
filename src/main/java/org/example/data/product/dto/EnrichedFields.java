package org.example.data.product.dto;

import com.chubb.na.domain.data.product.response.InsuredMatchingResponse;
import com.chubb.na.domain.data.product.response.ProducerSearchResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedFields {
	InsuredMatchingResponse insuredMatchingResponse;
	ProducerSearchResponse producerSearchResponse;
}
