package com.streaming.controller;

import com.streaming.repository.model.MetricsDocument;
import com.streaming.service.MetricsQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/metrics")
public class MetricsQueryController {

    @Autowired
    private MetricsQueryService metricsQueryService;

    @GetMapping("/today")
    public Flux<MetricsDocument> getToday(@RequestParam(required = false) String clientId) {
        return metricsQueryService.getMetricsForToday(clientId);
    }

    @GetMapping("/day")
    public Flux<MetricsDocument> getDay(
            @RequestParam String day, // format: yyyy-MM-dd
            @RequestParam(required = false) String clientId) {
        return metricsQueryService.getMetricsForDay(day, clientId);
    }

    @GetMapping("/past-hour")
    public Flux<MetricsDocument> getPastHour(@RequestParam(required = false) String clientId) {
        return metricsQueryService.getMetricsForThisPastHour(clientId);
    }

    @GetMapping("/range")
    public Flux<MetricsDocument> getRange(
            @RequestParam String day,         // e.g. 2025-05-24
            @RequestParam String from,        // e.g. 14:30
            @RequestParam String to,          // e.g. 16:45
            @RequestParam(required = false) String clientId) {
        return metricsQueryService.getMetricsInRange(day, from, to, clientId);
    }
}
