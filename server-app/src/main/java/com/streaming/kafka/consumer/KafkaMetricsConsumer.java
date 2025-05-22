package com.streaming.kafka.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens for EventOutcome messages and triggers settlement processing.
 */
@Component
public class KafkaMetricsConsumer {

    private final Logger log = LogManager.getLogger(getClass());

//    @Autowired
//    private SettlementService settlementService;
//
//    @KafkaListener(topics = "${spring.kafka.topics.aggregated}", groupId = "${spring.kafka.consumer.group-id}")
//    public void consume(EventOutcome eventOutcome) {
//        log.debug("Received Event Outcome with id = {}", eventOutcome.getEventId());
//        settlementService.processEventOutcome(eventOutcome);
//    }
}
