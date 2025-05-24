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

    public CompletableFuture<SendResult<String, byte[]>> sendAggregatedMetricsToTopic(byte[] payload) {
        CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(topic, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send aggregated metrics to Kafka", ex);
            } else {
                log.debug("Sent aggregated metrics to Kafka topic {}", topic);
            }
        });
        return future;
    }
}
