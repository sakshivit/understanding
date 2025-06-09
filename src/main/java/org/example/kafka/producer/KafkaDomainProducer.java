package org.example.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chubb.na.domain.kafka.config.KafkaConfig;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class KafkaDomainProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainProducer.class);

    @Inject
    KafkaConfig config;

    private KafkaProducer<String, String> producer = null;

    //@PostConstruct
    public void init() {
        this.producer = new KafkaProducer<String, String>(config.getProducerProperties());
    }

    public void publishMessage(String message, String topic) {
        log.info("PUBLISHING.....");
        if (null != producer) {
            producer.close();
        }
        init();
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, message);
        try {
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    log.info("------MESSAGE PUBLISHED TO---- : " + metadata.topic());
                } else {
                    log.error("-------EXCEPTION WHILE PUBLISHING MESSAGE TO ORCHESTRATION TOPIC-------" + exception);
                }

            });
        } catch (Exception e) {
            log.error("-------EXCEPTION WHILE PUBLISHING MESSAGE  -------" + e);
        }
    }


    @PreDestroy
    public void close() {
        producer.close();
    }

}
