package com.streaming.service;

import com.streaming.repository.MetricsRepository;
import com.streaming.repository.model.MetricsDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MetricsService {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MetricsRepository metricsRepository;

    public void processSparkMetric(Map<String, Object> metric) {
        List<String> enabledStrategies = getEnabledStrategies();
        var errors = schemaService.validate(metric, enabledStrategies);
        if (!errors.isEmpty()) {
            log.warn("Metric validation failed: {}", errors);
            // Optional: send invalid line to DLQ for review/re-processing
            // deadLetterQueueProducer.send(metric);
        } else {
            MetricsDocument doc = schemaService.toMetricsDocument(metric);
            metricsRepository.save(doc);
        }
    }

    public void processAggregatedMetrics(byte[] metrics) {
        List<String> enabledStrategies = getEnabledStrategies();
        var validatedLines = schemaService.parseAndValidateEachLine(metrics, enabledStrategies);

        List<MetricsDocument> validDocs = new ArrayList<>();
        for (var line : validatedLines) {
            if (line.errors().isEmpty()) {
                validDocs.add(schemaService.toMetricsDocument(line.raw()));
            } else {
                log.warn("Invalid metric line, skipping: {}", line.errors());
                // Optional: send invalid line to DLQ for review/re-processing
                // deadLetterQueueProducer.send(line.raw());
            }
        }

        if (!validDocs.isEmpty()) {
            metricsRepository.saveAll(validDocs);
        }
    }

    private List<String> getEnabledStrategies() {
        return policyService.fetchMetricsConfiguration()
                .blockOptional()
                .map(cfg -> cfg.getCollector().getEnabledStrategies())
                .orElse(List.of());
    }
}
