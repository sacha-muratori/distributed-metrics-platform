package com.streaming.controller;

import com.streaming.kafka.producer.KafkaAggregatedMetricsProducer;
import com.streaming.kafka.producer.KafkaSparkMetricsProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/metrics")
@Slf4j
public class MetricsController {

    @Autowired
    private KafkaSparkMetricsProducer sparkMetricsProducer;

    @Autowired
    private KafkaAggregatedMetricsProducer aggregatedMetricsProducer;

    // 1. Spark metrics - JSON map
    @PostMapping("/spark")
    public ResponseEntity<Void> receiveSparkMetric(@RequestBody Map<String, Object> metric) {
        try {
            sparkMetricsProducer.sendSparkMetricToTopic(metric); // async fire-and-forget
            return ResponseEntity.accepted().build(); // 202
        } catch (Exception e) {
            log.error("Error sending spark metric", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 2. Aggregated metrics - raw JSON payload as bytes
    @PostMapping("/aggregated")
    public ResponseEntity<Void> receiveAggregatedMetrics(@RequestBody byte[] payload) {
        try {
            aggregatedMetricsProducer.sendAggregatedMetricsToTopic(payload);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error sending aggregated metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 3. Retry batch - raw JSON payload as bytes
    @PostMapping("/retry")
    public ResponseEntity<Void> receiveRetryBatch(@RequestBody byte[] payload) {
        try {
            aggregatedMetricsProducer.sendAggregatedMetricsToTopic(payload);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error sending retry batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
