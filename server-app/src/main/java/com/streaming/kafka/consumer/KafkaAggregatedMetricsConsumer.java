package com.streaming.kafka.consumer;

import com.streaming.service.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer that listens for Spark messages and saves them in the database
 */
@Component
@Slf4j
public class KafkaAggregatedMetricsConsumer {

    @Autowired
    private MetricsService metricsService;

    @KafkaListener(topics = "${spring.kafka.topics.aggregated}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(byte[] metrics) {
        log.debug("Received aggregated metric from client");
        try {
            metricsService.processAggregatedMetrics(metrics);
        } catch (Exception e) {
            log.error("Error processing aggregated metric", e);
            // handle error, maybe dead letter queue
        }
    }
}
