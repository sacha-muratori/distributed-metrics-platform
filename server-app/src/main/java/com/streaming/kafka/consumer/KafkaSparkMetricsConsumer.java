package com.streaming.kafka.consumer;

import com.streaming.service.MetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer that listens for EventOutcome messages and triggers settlement processing.
 */
@Component
@Slf4j
public class KafkaSparkMetricsConsumer {

    @Autowired
    private MetricsService metricsService;

    @KafkaListener(topics = "${spring.kafka.topics.spark}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Map<String, Object> metric) {
        log.debug("Received spark metric from client");
        metricsService.processSparkMetric(metric);
    }
}
