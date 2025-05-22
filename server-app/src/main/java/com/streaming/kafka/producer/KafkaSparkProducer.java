package com.streaming.kafka.producer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka producer responsible for sending EventOutcome messages to the configured topic.
 */
@Component
@EnableKafka
public class KafkaSparkProducer {

    @Value("${spring.kafka.topics.spark}")
    private String topic;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public void sendEventOutcome(Map<String, Object> metric) {
//        log.debug("Sending Event Outcome with id = {} to Kafka Topic = {}", eventOutcome.getEventId(), topic);
        try {
            kafkaTemplate.send(topic, metric).get();
        } catch (Exception e) {
            throw new KafkaException("Failed to send event to Kafka", e);
        }
    }
}
