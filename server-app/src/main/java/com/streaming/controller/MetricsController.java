package com.streaming.controller;

import com.streaming.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

//    @PostMapping("/spark")
//    public Mono<Void> ingestSpark(@RequestBody Mono<MetricDTO> sparkMetric) {
//        return sparkMetric
//            .flatMap(metricsService::saveSparkMetric);
//    }
//
//    // or is it a List of Metrics ?
//    @PostMapping("/aggregated")
//    public Mono<ResponseEntity<Void>> ingestAggregated(@RequestBody Mono<MetricDTO> metric) {
//        return metric
//            .flatMap(metricsService::saveAggregatedMetric)
//            .thenReturn(ResponseEntity.ok().build());
//    }
//
//    // Different ways of
//    @GetMapping("/high-cpu")
//    public Flux<String> getClientsWithHighCpu(@RequestParam double threshold, @RequestParam double threshold) {
//        return metricsService.findHighCpuClients(threshold, Instant.now().minusSeconds(300))
//                .map(SparkMetricDTO::clientId);
//    }

}
