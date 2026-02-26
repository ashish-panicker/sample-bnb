package com.example.pricingservice.messaging.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        return new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
        // Explicitly tell the recoverer where to send the "failed" bytes
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));


        // Use Exponential Backoff to avoid "spamming" the logs during retries
        BackOff backOff = new FixedBackOff(2000L, 2); // 2 retries, 2 seconds apart

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // This stops the "failed to determine" error by telling Kafka
        // to stop trying to seek the record if the recovery itself fails
        handler.setAckAfterHandle(true);
        handler.addNotRetryableExceptions(NullPointerException.class, IllegalArgumentException.class);

        return handler;
    }
}
