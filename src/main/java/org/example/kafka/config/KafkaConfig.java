package org.example.kafka.config;

import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.chubb.na.domain.utils.Commons;

@ApplicationScoped
public class KafkaConfig {


    @ConfigProperty(name = "apache.kafka.booststrap.server")
    String bootstrapServer;

    @ConfigProperty(name = "group.id")
    String groupId;

    @ConfigProperty(name = "apache.kafka.protocol.config")
    String securityProtocol;

    @ConfigProperty(name = "apache.kafka.sasl.mechanism")
    String saslMechanism;

    @ConfigProperty(name = "apache.kafka.sasl.jaaas.config")
    String saslJaasConfig;

    @ConfigProperty(name = "apache.kafka.ssl.truststore.location")
    String trustStoreLocation;

    @ConfigProperty(name = "apache.kafka.ssl.truststore.pass")
    String trustStorePass;

    @ConfigProperty(name = "apache.kafka.consumer.auto.offset.reset")
    String offset;

    @ConfigProperty(name = "apache.kafka.consumer.auto.commit.interval.ms")
    String autoCommitInterval;

    @ConfigProperty(name = "apache.kafka.consumer.session.timeout.ms")
    String sessionTimeout;

    @ConfigProperty(name = "apache.kafka.consumer.max.poll.interval.ms")
    String maxPollInterval;

    @ConfigProperty(name = "apache.kafka.consumer.max.poll.records")
    int maxPollRecords;

    @ConfigProperty(name = "apache.kafka.producer.request.timeout.ms")
    String requestTimeout;

    @ConfigProperty(name = "apache.kafka.producer.retry.backoff.ms")
    String retryBackoff;

    @ConfigProperty(name = "apache.kafka.producer.retries")
    String retries;

    @ConfigProperty(name = "apache.kafka.producer.acks")
    String acks;


    public Properties getConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollInterval);
        getSecurityConfig(props);
        return props;
    }

    public Properties getProducerProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoff);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        getSecurityConfig(props);
        return props;
    }

    private Properties getSecurityConfig(Properties props) {
        props.put(Commons.SECURITY_PROTOCOL, securityProtocol);
        props.put(Commons.SASL_MECHANISM, saslMechanism);
        props.put(Commons.SASL_JAAS_CONFIG, saslJaasConfig);
        props.put(Commons.SSL_TRUSTSTORE_LOCATION, trustStoreLocation);
        props.put(Commons.SSL_TRUSTSTORE_PASS, trustStorePass);
        return props;
    }
}

