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
 * Kafka producer responsible for sending Sparks messages to the configured topic.
 */
@Component
@Slf4j
public class KafkaSparkMetricsProducer {

    @Value("${spring.kafka.topics.spark}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void sendSparkMetricToTopic(Map<String, Object> metric) {
        CompletableFuture<SendResult<String, Map<String, Object>>> future = kafkaTemplate.send(topic, metric);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send spark metric to Kafka", ex);
                // handle error, maybe dead letter queue
            } else {
                log.debug("Sent spark metric to Kafka topic {}", topic);
            }
        });
    }
}
