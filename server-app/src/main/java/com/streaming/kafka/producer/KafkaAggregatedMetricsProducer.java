package com.streaming.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer responsible for sending EventOutcome messages to the configured topic.
 */
@Component
@Slf4j
public class KafkaAggregatedMetricsProducer {

    @Value("${spring.kafka.topics.aggregated}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendAggregatedMetricsToTopic(byte[] metrics) {
        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(topic, metrics);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send aggregated metric to Kafka", ex);
                // handle error, maybe dead letter queue
            } else {
                log.debug("Sent aggregated metric to Kafka topic {}", topic);
            }
        });
    }
}
