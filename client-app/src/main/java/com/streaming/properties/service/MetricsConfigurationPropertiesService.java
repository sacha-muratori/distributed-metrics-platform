package com.streaming.properties.service;

import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;
import com.streaming.properties.model.MetricsConfigurationPropertiesHolder;
import com.streaming.properties.model.MetricsConfigurationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Consumer;

@Service
public class MetricsConfigurationPropertiesService {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private MetricsConfigurationPropertiesHolder configHolder;

    @Autowired
    private RestTemplate restTemplate;

    public boolean fetchAndUpdateConfig() {
        try {
            Map<String, Object> response = restTemplate.getForObject(
                    configHolder.getConfig().getThreshold().getConfigFetchUrl(), Map.class);

            if (response == null) {
                log.warn("Empty response when fetching config");
                return false;
            }

            MetricsConfigurationProperties currentConfig = configHolder.getConfig();
            MetricsConfigurationProperties newConfig = new MetricsConfigurationProperties();

            // Copy current config values
            newConfig.setEnabledStrategies(new ArrayList<>(currentConfig.getEnabledStrategies()));
            newConfig.setSchedule(copySchedule(currentConfig.getSchedule()));
            newConfig.setThreshold(copyThreshold(currentConfig.getThreshold()));
            newConfig.setClient(copyClient(currentConfig.getClient()));

            // Update values from response
            updateStrategies(newConfig, (List<String>) response.get("strategies"));
            updateSchedule(newConfig.getSchedule(), (Map<String, Object>) response.get("schedule"));
            updateThreshold(newConfig.getThreshold(), (Map<String, Object>) response.get("threshold"));
            updateClient(newConfig.getClient(), (Map<String, Object>) response.get("client"));

            // Atomic update if changed
            if (!newConfig.equals(currentConfig)) {
                configHolder.updateConfig(newConfig);
                log.info("Metrics configuration updated atomically");
                return true;
            }

            log.debug("Fetched config is identical to current config. No update necessary.");
            return false;

        } catch (Exception e) {
            log.warn("Failed to fetch config from server: {}", e.getMessage(), e);
            return false;
        }
    }

    private MetricsConfigurationProperties.Schedule copySchedule(MetricsConfigurationProperties.Schedule oldSchedule) {
        MetricsConfigurationProperties.Schedule newSchedule = new MetricsConfigurationProperties.Schedule();
        newSchedule.setInitialDelayMs(oldSchedule.getInitialDelayMs());
        newSchedule.setSparksRateMs(oldSchedule.getSparksRateMs());
        newSchedule.setFixedRateMs(oldSchedule.getFixedRateMs());
        return newSchedule;
    }

    private MetricsConfigurationProperties.Threshold copyThreshold(MetricsConfigurationProperties.Threshold oldThreshold) {
        MetricsConfigurationProperties.Threshold newThreshold = new MetricsConfigurationProperties.Threshold();
        newThreshold.setHighCpuPercentage(oldThreshold.getHighCpuPercentage());
        newThreshold.setConfigFetchUrl(oldThreshold.getConfigFetchUrl());
        newThreshold.setFetchIntervalMs(oldThreshold.getFetchIntervalMs());
        return newThreshold;
    }


    private MetricsConfigurationProperties.Client copyClient(MetricsConfigurationProperties.Client oldThreshold) {
        MetricsConfigurationProperties.Client newClient = new MetricsConfigurationProperties.Client();
        newClient.setRegistrationUrl(oldThreshold.getRegistrationUrl());
        newClient.setRefreshIntervalMs(oldThreshold.getRefreshIntervalMs());
        return newClient;
    }

    private void updateStrategies(MetricsConfigurationProperties config, List<String> strategyListRaw) {
        if (strategyListRaw == null) return;

        List<String> validStrategies = new ArrayList<>();
        List<String> invalidStrategies = new ArrayList<>();

        for (String strategyName : strategyListRaw) {
            if (strategyName != null) {
                if (MetricsCollectorStrategyType.fromName(strategyName).isPresent()) {
                    validStrategies.add(strategyName);
                } else {
                    invalidStrategies.add(strategyName);
                }
            }
        }

        if (!validStrategies.isEmpty()) {
            config.setEnabledStrategies(validStrategies);
            log.debug("Updated strategies to {}", validStrategies);
        }

        if (!invalidStrategies.isEmpty()) {
            log.warn("Ignored unknown strategy names due to mismatch: {}.", invalidStrategies);
        }
    }


    private void updateSchedule(MetricsConfigurationProperties.Schedule schedule, Map<String, Object> scheduleMap) {
        if (scheduleMap == null) return;
        setIfPresent(scheduleMap, "initialDelayMs", val -> schedule.setInitialDelayMs(toLong(val)));
        setIfPresent(scheduleMap, "sparksRateMs", val -> schedule.setSparksRateMs(toLong(val)));
        setIfPresent(scheduleMap, "fixedRateMs", val -> schedule.setFixedRateMs(toLong(val)));
    }

    private void updateThreshold(MetricsConfigurationProperties.Threshold threshold, Map<String, Object> thresholdMap) {
        if (thresholdMap == null) return;
        setIfPresent(thresholdMap, "highCpuPercentage", val -> threshold.setHighCpuPercentage(toInt(val)));
        setIfPresent(thresholdMap, "configFetchUrl", val -> threshold.setConfigFetchUrl(val.toString()));
        setIfPresent(thresholdMap, "fetchIntervalMs", val -> threshold.setFetchIntervalMs(toLong(val)));
    }

    private void updateClient(MetricsConfigurationProperties.Client client, Map<String, Object> clientMap) {
        if (clientMap == null) return;
        setIfPresent(clientMap, "registrationUrl", val -> client.setRegistrationUrl(val.toString()));
        setIfPresent(clientMap, "refreshIntervalMs", val -> client.setRefreshIntervalMs(toLong(val)));
    }

    private void setIfPresent(Map<String, Object> map, String key, Consumer<Object> setter) {
        if (map.containsKey(key)) {
            try {
                setter.accept(map.get(key));
                log.debug("Updated {} to {}", key, map.get(key));
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
