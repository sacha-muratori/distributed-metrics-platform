package com.streaming.metrics.collector.service;

import com.streaming.client.identity.model.ClientIdentity;
import com.streaming.client.identity.store.ClientIdentityStoreService;
import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyFactory;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType.ALL_STRATEGY_NAMES;

@Service
@Slf4j
public class SystemMetricsCollectorService {

    @Autowired
    private ConfigurationPropertiesHolder configHolder;

    @Autowired
    private ClientIdentityStoreService clientIdentityStoreService;

    public Map<String, Object> collectMetrics() {
        log.debug("Starting metrics collection");

        List<String> enabledNames = configHolder.getMetricsConfigRef().getCollector().getEnabledStrategies();
        log.debug("Enabled strategies: {}", String.join(", ", enabledNames));

        List<MetricsCollectorStrategy> strategies = resolveStrategies(enabledNames);
        log.debug("Resolved strategies: {}", strategies.stream().map(MetricsCollectorStrategy::getName).collect(Collectors.joining(", ")));

        Map<String, Object> metrics = new HashMap<>();
        insertClientIdentityOnMetrics(metrics);
        log.debug("Added client information: {}", metrics);

        collectFromStrategies(strategies, metrics);
        log.debug("Collected metrics: {}", metrics);

        insertTimestampOnMetrics(metrics);
        log.debug("Added Timestamp: {}", metrics);

        return metrics;
    }

    private List<MetricsCollectorStrategy> resolveStrategies(List<String> names) {
        List<MetricsCollectorStrategy> strategies = new ArrayList<>();
        for (String name : names) {
            MetricsCollectorStrategyType.fromName(name).ifPresentOrElse(type -> {
                MetricsCollectorStrategyFactory.create(type).ifPresentOrElse(
                        strategies::add,
                        () -> log.warn("No strategy implementation for '{}'", type.getName())
                );
            }, () -> {
                log.warn("Unknown strategy name '{}'. Available: {}", name, ALL_STRATEGY_NAMES);
            });
        }
        return strategies;
    }

    private void collectFromStrategies(List<MetricsCollectorStrategy> strategies, Map<String, Object> metrics) {
        // Collecting data based on strategies defined in configuration
        for (MetricsCollectorStrategy strategy : strategies) {
            try {
                metrics.putAll(strategy.collect());
            } catch (Exception e) {
                log.warn("Error collecting metrics from {}", strategy.getClass().getSimpleName(), e);
            }
        }
    }

    private void insertTimestampOnMetrics(Map<String, Object> metrics) {
        metrics.put("timestamp", Instant.now().toString());
    }

    private void insertClientIdentityOnMetrics(Map<String, Object> metrics) {
        ClientIdentity clientIdentity = clientIdentityStoreService.load();
        metrics.put("fingerprint", clientIdentity.getFingerprint());
        metrics.put("clientId", clientIdentity.getClientId());
    }
}
