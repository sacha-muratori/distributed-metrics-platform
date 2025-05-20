package com.streaming.configuration.properties.scheduler;

import com.streaming.configuration.properties.model.PolicyConfigurationProperties;
import com.streaming.configuration.properties.service.ConfigurationPropertiesService;
import com.streaming.metrics.collector.scheduler.SystemMetricsCollectorScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationPropertiesScheduler {

    @Autowired
    private ConfigurationPropertiesService propertiesService;

    @Autowired
    private PolicyConfigurationProperties policyConfigurationProperties;

    @Autowired
    private SystemMetricsCollectorScheduler metricsCollectorScheduler;

    @Scheduled(fixedRateString = "#{@policyConfigurationProperties.fetchUrl}")
    public void refreshPropertiesFromServer() {
        // This Scheduler is static, it's updating the metrics properties and in turn could change the intervals
        // of the dynamic schedulers for the metrics collection (spark, aggregated, retry)
        boolean updated = propertiesService.fetchAndUpdateMetricsConfigs();
        if (updated) {
            metricsCollectorScheduler.rescheduleIfNeeded();
        }
    }
}


