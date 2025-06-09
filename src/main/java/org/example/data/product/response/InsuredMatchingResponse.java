package org.example.data.product.response;

import java.util.List;

import com.chubb.na.domain.data.product.dto.InsuredMatchingOutput;
import com.chubb.na.domain.data.product.request.OriginalRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuredMatchingResponse {

	private OriginalRequest originalRequest;
	private List<InsuredMatchingOutput> output;

}
