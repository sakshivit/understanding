package org.example.service;

import java.util.HashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.chubb.ipa.graph.user.model.UserInfoCollection;
import com.chubb.ipa.graph.user.service.impl.UserGraphService;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CISAUserGraphService extends UserGraphService {

    @ConfigProperty(name = "graph.api.tenant")
    String tenantId;
    @ConfigProperty(name = "graph.client.id")
    String clientId;
    @ConfigProperty(name = "graph.client.secret")
    String clientSecret;

    public CISAUserGraphService(String tenantId, String clientId, String clientSecret) {
        super(tenantId, clientId, clientSecret);
    }

    public CISAUserGraphService(String tenantId, String clientId, String clientSecret, long retryPeriod,
                                long retryMaxPeriod, int retryMaxAttempt) {
        super(tenantId, clientId, clientSecret, retryPeriod, retryMaxPeriod, retryMaxAttempt);
    }

    public UserInfoCollection getUserInfo(String searchParam, String value) {
        UserInfoCollection userInfoCollection = null;
        HashMap<String, Object> map = new HashMap<>();
        map.put("$search", "\"" + searchParam + ":" + value + "\"");
        userInfoCollection = this.getUserInfoClient().getUserInfo(map);
        return userInfoCollection;
    }

}
