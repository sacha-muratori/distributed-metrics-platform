package com.streaming.configuration.properties.scheduler;

import com.streaming.configuration.properties.model.PolicyConfigurationProperties;
import com.streaming.configuration.properties.service.ConfigurationPropertiesService;
import com.streaming.metrics.collector.scheduler.SystemMetricsCollectorScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConfigurationPropertiesScheduler {

    @Autowired
    private PolicyConfigurationProperties policyConfigurationProperties;

    @Autowired
    private ConfigurationPropertiesService propertiesService;

    @Autowired
    private SystemMetricsCollectorScheduler metricsCollectorScheduler;

    // This Scheduler is static, it's updating the metrics properties and in turn could change the intervals
    // of the dynamic schedulers for the metrics collection (spark, aggregated, retry)
    @Scheduled(
            fixedRateString = "#{@policyConfigurationProperties.schedulerIntervalMs}",
            initialDelayString = "#{@policyConfigurationProperties.initialDelayMs}"
    )
    public void refreshPropertiesFromServer() {
        propertiesService.fetchAndUpdateMetricsConfigsReactive()
                .doOnNext(updated -> {
                    if (updated) {
                        metricsCollectorScheduler.rescheduleMetricsCollectionTasks();
                    }
                })
                .doOnError(e -> log.warn("Failed to refresh config: {}", e.getMessage()))
                .subscribe();
    }
}


