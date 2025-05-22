package com.streaming.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@Slf4j
public class MetricsController {

    // 1. Spark: fire-and-forget, JSON body is a metric map
    @PostMapping("/spark")
    public ResponseEntity<Void> receiveSparkMetric(@RequestBody Map<String, Object> metric) {
        log.info("Received spark metric: {}", metric);
        // Fire-and-forget — processing could be async
        return ResponseEntity.accepted().build(); // HTTP 202
    }

    // 2. Aggregated: receive raw JSON payload (file content)
    @PostMapping("/aggregated")
    public ResponseEntity<Void> receiveAggregatedMetrics(@RequestBody byte[] payload) {
        String jsonString = new String(payload, StandardCharsets.UTF_8);
        log.info("Received aggregated metrics batch: {}", jsonString);
        // You can store or parse jsonString here
        return ResponseEntity.accepted().build(); // HTTP 202
    }

    // 3. Retry: same structure as aggregated, just from archive
    @PostMapping("/retry")
    public ResponseEntity<Void> receiveRetryBatch(@RequestBody byte[] payload) {
        String jsonString = new String(payload, StandardCharsets.UTF_8);
        log.info("Received retry batch: {}", jsonString);
        // Retry logic can be triggered here (e.g., parse again or re-store)
        return ResponseEntity.accepted().build(); // HTTP 202
    }
}
