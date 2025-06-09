package org.example.data.product.restclient.impl;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.chubb.na.domain.data.product.dto.MultipartBody;
import com.chubb.na.domain.data.product.restclient.AdlsClient;

@ApplicationScoped
public class AdlsAPIAccessor {

    @RestClient
    AdlsClient adlsClient;

    public String downloadFrmAdls(String uri) {
        return adlsClient.downloadFile(uri);
    }

    public byte[] downloadFrmAdlsV1(String uri) {
        return adlsClient.downloadFile(uri).getBytes();
    }

    public String uploadFileToAdls(MultipartBody body) {
        return adlsClient.uploadFile(body);
    }
}
