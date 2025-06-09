package org.example.config;

import java.io.IOException;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DroolsConfig {

    private static KieServices kieServices;
    private KieContainer kieContainer = null;

    private static final Logger LOG = LoggerFactory.getLogger(DroolsConfig.class);

    public DroolsConfig() {
        kieServices = KieServices.Factory.get();
    }

    private KieFileSystem getKieFileSystem(String fileName) throws IOException {

        KieFileSystem kieFS = kieServices.newKieFileSystem();
        try {
            Resource resource = ResourceFactory.newClassPathResource("rule-files/" + fileName, getClass());
            kieFS.write(resource);
        } catch (Exception e) {
            LOG.error("Exception occurred while adding rules file to KieFileSystem: " + e);
            //e.printStackTrace();
        }
        return kieFS;
    }

    public KieContainer getKieContainer(String fileName) throws IOException {
        KieBuilder kb = kieServices.newKieBuilder(getKieFileSystem(fileName));
        kb.buildAll();
        Results results = kb.getResults();

        if (results.hasMessages(Message.Level.ERROR)) {
            LOG.error("Errors occurred while Kie env building: " + results.getMessages());
            throw new IllegalStateException("IllegalStateException occurred while Kie env building!");
        }

        KieContainer kContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        return kContainer;
    }

    public KieSession getKieSession(String fileName) throws IOException {
        kieContainer = getKieContainer(fileName);
        KieSessionConfiguration configuration = kieServices.newKieSessionConfiguration();
        configuration.setProperty("drools.dialect.mvel.strict", "false");
        return kieContainer.newKieSession(configuration);
    }

    public void executeRules(Object object, String fileName) {
        KieSession kSession = null;
        FactHandle handle = null;

        try {
            kSession = getKieSession(fileName);
            handle = kSession.insert(object);
            kSession.fireAllRules();
            kSession.delete(handle);
            kSession.destroy();
        } catch (Exception e) {
            LOG.error("Exception occured while executing rules: " + e);
        }
    }
}
