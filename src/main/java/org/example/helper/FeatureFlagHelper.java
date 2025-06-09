package org.example.helper;

import com.chubb.na.domain.data.product.response.ODSResponse;
import com.chubb.na.domain.data.product.restclient.ODSClient;
import com.chubb.na.domain.service.ProcessSubmissionService;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Singleton
public class FeatureFlagHelper {

    @RestClient
    private ODSClient odsClient;

    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagHelper.class);

    public String getFeatureFlag(String key) {
        return Optional.ofNullable(readFeatureFlag(key))
                .map(String::trim)
                .orElse(null);
    }

    private String readFeatureFlag(String key) {
        ODSResponse odsRes = odsClient.getConfiguration(key);
        return odsRes.getConfigurationValue();
    }

    public boolean getFeatureFlagAsBoolean(String key) {
        boolean skipStp = true;

        try {
            skipStp = Optional.ofNullable(getFeatureFlag(key))
                    .map(flag -> flag.equalsIgnoreCase("true"))
                    .orElse(false);
        } catch (Exception e) {
            logger.error("----- EXCEPTION WHILE GETTING SKIP STP FLAG: " + e.getMessage() + " -----");
        }

        return skipStp;
    }
}
