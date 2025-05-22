package com.streaming.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Component;

/**
 * Kafka producer responsible for sending EventOutcome messages to the configured topic.
 */
@Component
@EnableKafka
public class KafkaMetricsProducer {

    @Value("${spring.kafka.topic}")
    private String topic;

//    @Autowired
//    private KafkaTemplate<String, EventOutcome> kafkaTemplate;
//
//    public void sendEventOutcome(EventOutcome eventOutcome) {
//        log.debug("Sending Event Outcome with id = {} to Kafka Topic = {}", eventOutcome.getEventId(), topic);
//        try {
//            kafkaTemplate.send(topic, eventOutcome).get();
//        } catch (Exception e) {
//            throw new KafkaException("Failed to send event to Kafka", e);
//        }
//    }
}
