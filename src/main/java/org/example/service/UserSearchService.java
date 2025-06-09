package org.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.chubb.na.domain.utils.Commons;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.chubb.ipa.graph.user.model.UserInfoCollection;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserSearchService {

    @ConfigProperty(name = "graph.api.tenant")
    String tenantId;
    @ConfigProperty(name = "graph.client.id")
    String clientId;
    @ConfigProperty(name = "graph.client.secret")
    String clientSecret;

    private CISAUserGraphService userGraphService;

    private CISAUserGraphService getUserGraphService() {
        if(userGraphService == null){
            userGraphService = new CISAUserGraphService(tenantId, clientId, clientSecret);
        }
        return userGraphService;
    }

    public UserInfoCollection getInfoFromMSExchange(String param, String value) {
        UserInfoCollection uwDetails = getUserGraphService().getUserInfo(param, value);
        return uwDetails;
    }

    public String getEmailFromMSExchange(String userName, List<String> underwriternames) {
        UserInfoCollection ud = getInfoFromMSExchange("displayName", userName);

        String email = "";
        if (ud != null & ud.getValue().size() != 0) {

            List<UserInfoCollection.UserDetails> userDetails = ud.getValue();
            if (userDetails.size() >1 && !CollectionUtils.isEmpty(underwriternames)) {
                Outer: for (UserInfoCollection.UserDetails details : userDetails) {
                    for (String name : underwriternames) {
                        if (details.getJobTitle().contains(name) && details.getMail().toLowerCase().contains(Commons.CHUBB_EMAIL.toLowerCase())) {
                            email = details.getMail();
                            break Outer;
                        }
                    }
                }
            } else {
                for (UserInfoCollection.UserDetails details : userDetails) {
                    if (details.getMail().toLowerCase().contains(Commons.CHUBB_EMAIL.toLowerCase())) {
                        email = details.getMail();
                        break;
                    }
                }
            }
        }
        return email;
    }

}

