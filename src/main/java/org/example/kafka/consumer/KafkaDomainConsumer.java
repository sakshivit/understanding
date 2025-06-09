package org.example.kafka.consumer;

import java.time.Duration;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.chubb.na.domain.helper.JsonHelper;
import com.chubb.na.domain.kafka.config.KafkaConfig;
import com.chubb.na.domain.service.ProcessSubmissionService;
import com.chubb.na.domain.utils.Commons;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

@ApplicationScoped
public class KafkaDomainConsumer {

    @Inject
    ProcessSubmissionService processSubmisionService;

    @Inject
    Jsonb jsonb;

    @Inject
    JsonHelper jsonHelper;

    private KafkaConsumer<String, String> kafkaConsumer;
    @Inject
    KafkaConfig config;
    @ConfigProperty(name = "rc.topic")
    String retailCommercialTopic;
    @ConfigProperty(name = "cib.topic")
    String cibTopic;
    @ConfigProperty(name = "node.keyword")
    String nodeKeyword;

    @PostConstruct
    public void init() {


        kafkaConsumer = new KafkaConsumer<String, String>(config.getConsumerProperties());
        kafkaConsumer.subscribe(Arrays.asList(this.retailCommercialTopic, this.cibTopic));

    }

    public void consumeMessages(@Observes StartupEvent ev) {
        while (true) {

            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                String intakeMessage = record.value();
                String emailSubject = jsonHelper.getEmailSubjectFromMessage(intakeMessage);
                if (StringUtils.isBlank(nodeKeyword) || StringUtils.contains(emailSubject, nodeKeyword)) {
                    if (StringUtils.equalsAnyIgnoreCase(record.topic(), this.retailCommercialTopic)) {
                        processSubmisionService.processSubmission(intakeMessage, Commons.RC_LOB);
                    } else {
                        processSubmisionService.processSubmission(intakeMessage, Commons.CIB_LOB);
                    }
                }
            });

        }
    }
    @PreDestroy
    public void close() {
        kafkaConsumer.close();
    }
}
