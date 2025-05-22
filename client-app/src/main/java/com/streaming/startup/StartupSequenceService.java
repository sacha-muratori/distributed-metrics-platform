package com.streaming.startup;

import com.streaming.client.registration.service.ClientRegistrationService;
import com.streaming.configuration.properties.service.ConfigurationPropertiesService;
import com.streaming.metrics.collector.scheduler.SystemMetricsCollectorScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupSequenceService {

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Autowired
    private ConfigurationPropertiesService metricsConfigurationPropertiesService;

    @Autowired
    private SystemMetricsCollectorScheduler systemMetricsCollectorScheduler;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        // Fetch Client, Schedule and Strategies Configuration from Server
        metricsConfigurationPropertiesService.fetchAndUpdateMetricsConfigsReactive().block();  // logs warning if fails, scheduler will run anyway

        // Register Client as new device towards Server
        clientRegistrationService.registerClient().block();                                    // logs warning if fails, scheduler will run anyway

        // Start Scheduler Collectors regardless of what failed — we have fallbacks for registering and properties.
        systemMetricsCollectorScheduler.scheduleTasks();
    }
}
