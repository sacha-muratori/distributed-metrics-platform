package com.streaming.kafka.consumer;

import com.streaming.service.MetricsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer that listens for EventOutcome messages and triggers settlement processing.
 */
@Component
public class KafkaSparkConsumer {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private MetricsService metricsService;

    @KafkaListener(topics = "${spring.kafka.topics.spars}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(Map<String, Object> rawMetric) {
//        log.debug("Received Event Outcome with client id = {}", rawMetric.getEventId());

//        metricsService.processRawMetric(rawMetric);
    }
}
