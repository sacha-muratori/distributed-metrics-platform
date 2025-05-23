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
            throw new IllegalArgumentException("Invalid metric input: " + errors);
        }
        MetricsDocument doc = schemaService.toMetricsDocument(metric);
        metricsRepository.save(doc);
    }

    public void processAggregatedMetrics(byte[] metrics) {
        List<String> enabledStrategies = getEnabledStrategies();

        // Parse & validate all lines once, get raw maps + validation results
        var validatedLines = schemaService.parseAndValidateEachLine(metrics, enabledStrategies);
        for (var line : validatedLines) {
            if (!line.errors().isEmpty()) {
                log.warn("Validation failed for metrics line: {}", line.errors());
                throw new IllegalArgumentException("Invalid metric input: " + line.errors());
            }
        }
        List<MetricsDocument> validDocs = schemaService.toMetricsDocumentList(metrics);

        // Save all valid metrics documents to MongoDB collection
        metricsRepository.saveAll(validDocs);
    }

    private List<String> getEnabledStrategies() {
        return policyService.fetchMetricsConfiguration()
                .blockOptional()
                .map(cfg -> cfg.getCollector().getEnabledStrategies())
                .orElse(List.of());
    }
}
