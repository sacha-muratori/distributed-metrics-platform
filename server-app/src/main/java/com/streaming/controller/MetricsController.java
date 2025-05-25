package com.streaming.controller;

import com.streaming.kafka.producer.KafkaAggregatedMetricsProducer;
import com.streaming.kafka.producer.KafkaSparkMetricsProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<Object>> receiveSparkMetric(@RequestBody Map<String, Object> metric) {
        return Mono.fromFuture(sparkMetricsProducer.sendSparkMetricToTopic(metric))
                .doOnSuccess(result -> log.debug("Sent spark metric to Kafka topic"))
                .then(Mono.just(ResponseEntity.accepted().build()))
                .onErrorResume(e -> {
                    log.error("Error sending spark metric", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // 2. Aggregated metrics - raw JSON payload as bytes
    @PostMapping("/aggregated")
    public Mono<ResponseEntity<Object>> receiveAggregatedMetrics(@RequestBody byte[] payload) {
        return Mono.fromFuture(aggregatedMetricsProducer.sendAggregatedMetricsToTopic(payload))
                .doOnSuccess(result -> log.debug("Sent aggregated metrics to Kafka topic"))
                .then(Mono.just(ResponseEntity.accepted().build()))
                .onErrorResume(e -> {
                    log.error("Error sending aggregated metrics", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // 3. Retry batch - raw JSON payload as bytes
    @PostMapping("/retry")
    public Mono<ResponseEntity<Object>> receiveRetryBatch(@RequestBody byte[] payload) {
        return Mono.fromFuture(aggregatedMetricsProducer.sendAggregatedMetricsToTopic(payload))
                .doOnSuccess(result -> log.debug("Sent retry batch to Kafka topic"))
                .then(Mono.just(ResponseEntity.accepted().build()))
                .onErrorResume(e -> {
                    log.error("Error sending retry batch", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
