package com.streaming.properties.scheduler;

import com.streaming.properties.model.MetricsConfigurationProperties;
import com.streaming.metrics.scheduler.SystemMetricsCollectorScheduler;
import com.streaming.properties.service.MetricsConfigurationPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetricsConfigurationPropertiesScheduler {

    @Autowired
    private MetricsConfigurationPropertiesService propertiesService;

    @Autowired
    private MetricsConfigurationProperties metricsProperties;

    @Autowired
    private SystemMetricsCollectorScheduler metricsCollectorScheduler;

    @Scheduled(fixedRateString = "#{@metricsProperties.threshold.fetchIntervalMs}")
    public void refreshPropertiesFromServer() {
        boolean updated = propertiesService.fetchAndUpdateConfig();
        if (updated) {
            metricsCollectorScheduler.rescheduleIfNeeded();
        }
    }
}


