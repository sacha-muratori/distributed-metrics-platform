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
        log.debug("Attempting processing Spark Metric");

        List<String> enabledStrategies = getEnabledStrategies();
        var errors = schemaService.validate(metric, enabledStrategies);
        if (!errors.isEmpty()) {
            log.warn("Metric validation failed: {}", errors);
            // Optional: send invalid line to DLQ for review/re-processing
            // deadLetterQueueProducer.send(metric);
        } else {
            log.debug("Metric validation successful for metric: ", metric);
            MetricsDocument doc = schemaService.toMetricsDocument(metric);

            log.debug("Metric de-serialization successful, saving metric to Collection: ", metric);
            metricsRepository.save(doc)
                    .doOnSuccess(docSaved -> log.debug("Document saved: {}", docSaved.getId()))
                    .doOnError(error -> log.error("Error saving document", error))
                    .subscribe();

            // and/or other processing for alerts
        }
    }

    public void processAggregatedMetrics(byte[] metrics) {
        log.debug("Attempting processing Aggregated Metric");
        List<String> enabledStrategies = getEnabledStrategies();
        var validatedLines = schemaService.parseAndValidateEachLine(metrics, enabledStrategies);

        List<MetricsDocument> validDocs = new ArrayList<>();
        for (var line : validatedLines) {
            if (!line.errors().isEmpty()) {
                log.warn("Invalid metric line, skipping: {}", line.errors());
                // Optional: send invalid line to DLQ for review/re-processing
                // deadLetterQueueProducer.send(line.raw());
            } else {
                validDocs.add(schemaService.toMetricsDocument(line.raw()));
            }
        }

        if (!validDocs.isEmpty()) {
            log.debug("Metric de-serialization successful for aggregated metrics, saving to Collection");
            metricsRepository.saveAll(validDocs)
                    .doOnComplete(() -> log.debug("All documents saved"))
                    .doOnError(error -> log.error("Error saving documents", error))
                    .subscribe();
        }
    }

    private List<String> getEnabledStrategies() {
        return policyService.fetchMetricsConfiguration()
                .blockOptional()
                .map(cfg -> cfg.getCollector().getEnabledStrategies())
                .orElse(List.of());
    }
}
