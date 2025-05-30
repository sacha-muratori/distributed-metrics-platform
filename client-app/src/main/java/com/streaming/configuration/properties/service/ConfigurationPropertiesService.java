package com.streaming.configuration.properties.service;

import com.streaming.configuration.properties.model.MetricsConfigurationProperties;
import com.streaming.configuration.properties.model.PolicyConfigurationProperties;
import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class ConfigurationPropertiesService {

    @Autowired
    private ConfigurationPropertiesHolder configHolder;

    @Autowired
    private PolicyConfigurationProperties policyConfigurationProperties;

    @Autowired
    private WebClient webClient;

    public Mono<Boolean> fetchAndUpdateMetricsConfigsReactive() {
        return webClient.get()
                .uri(policyConfigurationProperties.getFetchUrl())
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> log.debug("Fetched config: {}", response))
                .map(this::fetchAndUpdateMetricsConfig)
                .doOnNext(updated -> log.debug("Metrics config updated: {}", updated))
                .onErrorReturn(false);
    }

    public boolean fetchAndUpdateMetricsConfig(Map<String, Object> response) {
        MetricsConfigurationProperties current = configHolder.getMetricsConfigRef();
        MetricsConfigurationProperties updated = deepCopy(current);

        Map<String, Object> collectorMap = (Map<String, Object>) response.get("collector");
        if (collectorMap != null) {
            updateStrategies(updated.getCollector(), (List<String>) collectorMap.get("enabledStrategies"));
            updateThreshold(updated.getCollector().getThreshold(), (Map<String, Object>) collectorMap.get("threshold"));
            updateSpark(updated.getCollector().getSpark(), (Map<String, Object>) collectorMap.get("spark"));
            updateAggregated(updated.getCollector().getAggregated(), (Map<String, Object>) collectorMap.get("aggregated"));
            updateRetry(updated.getCollector().getRetry(), (Map<String, Object>) collectorMap.get("retry"));
        }

        // Compare whole config for equality
        if (!updated.equals(current)) {
            configHolder.setMetricsConfigRef(updated);
            log.debug("Metrics configuration updated atomically");
            return true;
        } else {
            log.debug("Metrics config unchanged");
            return false;
        }
    }

    private MetricsConfigurationProperties deepCopy(MetricsConfigurationProperties original) {
        MetricsConfigurationProperties copy = new MetricsConfigurationProperties();

        MetricsConfigurationProperties.Collector collectorCopy = new MetricsConfigurationProperties.Collector();
        collectorCopy.setEnabledStrategies(new ArrayList<>(original.getCollector().getEnabledStrategies()));

        collectorCopy.setThreshold(copyThreshold(original.getCollector().getThreshold()));
        collectorCopy.setSpark(copySpark(original.getCollector().getSpark()));
        collectorCopy.setAggregated(copyAggregated(original.getCollector().getAggregated()));
        collectorCopy.setRetry(copyRetry(original.getCollector().getRetry()));

        copy.setCollector(collectorCopy);

        return copy;
    }

    private MetricsConfigurationProperties.Threshold copyThreshold(MetricsConfigurationProperties.Threshold old) {
        MetricsConfigurationProperties.Threshold copy = new MetricsConfigurationProperties.Threshold();
        copy.setHighCpuPercentage(old.getHighCpuPercentage());
        return copy;
    }

    private MetricsConfigurationProperties.Spark copySpark(MetricsConfigurationProperties.Spark old) {
        MetricsConfigurationProperties.Spark copy = new MetricsConfigurationProperties.Spark();
        copy.setInitialDelayMs(old.getInitialDelayMs());
        copy.setSchedulerIntervalMs(old.getSchedulerIntervalMs());
        copy.setSparkAlertUrl(old.getSparkAlertUrl());
        return copy;
    }

    private MetricsConfigurationProperties.Aggregated copyAggregated(MetricsConfigurationProperties.Aggregated old) {
        MetricsConfigurationProperties.Aggregated copy = new MetricsConfigurationProperties.Aggregated();
        copy.setInitialDelayMs(old.getInitialDelayMs());
        copy.setSchedulerIntervalMs(old.getSchedulerIntervalMs());
        copy.setAggregatedUrl(old.getAggregatedUrl());
        return copy;
    }

    private MetricsConfigurationProperties.Retry copyRetry(MetricsConfigurationProperties.Retry old) {
        MetricsConfigurationProperties.Retry copy = new MetricsConfigurationProperties.Retry();
        copy.setInitialDelayMs(old.getInitialDelayMs());
        copy.setSchedulerIntervalMs(old.getSchedulerIntervalMs());
        return copy;
    }

    private void updateStrategies(MetricsConfigurationProperties.Collector collector, List<String> raw) {
        if (raw == null) return;

        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();

        for (String name : raw) {
            if (name != null && MetricsCollectorStrategyType.fromName(name).isPresent()) {
                valid.add(name);
            } else {
                invalid.add(name);
            }
        }

        if (!valid.isEmpty()) {
            collector.setEnabledStrategies(valid);
            log.debug("Updated strategies: {}", valid);
        }

        if (!invalid.isEmpty()) {
            log.warn("Invalid strategies ignored: {}", invalid);
        }
    }

    private void updateThreshold(MetricsConfigurationProperties.Threshold threshold, Map<String, Object> map) {
        if (map == null) return;
        setIfPresent(map, "highCpuPercentage", v -> threshold.setHighCpuPercentage(toInt(v)));
    }

    private void updateSpark(MetricsConfigurationProperties.Spark spark, Map<String, Object> map) {
        if (map == null) return;
        setIfPresent(map, "schedulerIntervalMs", v -> spark.setSchedulerIntervalMs(toLong(v)));
        setIfPresent(map, "initialDelayMs", v -> spark.setInitialDelayMs(toLong(v)));
        setIfPresent(map, "sparkAlertUrl", v -> spark.setSparkAlertUrl(v.toString()));
    }

    private void updateAggregated(MetricsConfigurationProperties.Aggregated agg, Map<String, Object> map) {
        if (map == null) return;
        setIfPresent(map, "schedulerIntervalMs", v -> agg.setSchedulerIntervalMs(toLong(v)));
        setIfPresent(map, "initialDelayMs", v -> agg.setInitialDelayMs(toLong(v)));
        setIfPresent(map, "aggregatedUrl", v -> agg.setAggregatedUrl(v.toString()));
    }

    private void updateRetry(MetricsConfigurationProperties.Retry retry, Map<String, Object> map) {
        if (map == null) return;
        setIfPresent(map, "schedulerIntervalMs", v -> retry.setSchedulerIntervalMs(toLong(v)));
        setIfPresent(map, "initialDelayMs", v -> retry.setInitialDelayMs(toLong(v)));
    }

    private void setIfPresent(Map<String, Object> map, String key, Consumer<Object> setter) {
        if (map.containsKey(key)) {
            try {
                setter.accept(map.get(key));
                log.debug("Set {} to {}", key, map.get(key));
            } catch (Exception e) {
                log.warn("Failed to update {}: {}", key, e.getMessage());
            }
        }
    }

    private long toLong(Object obj) {
        return Long.parseLong(obj.toString());
    }

    private int toInt(Object obj) {
        return Integer.parseInt(obj.toString());
    }
}
