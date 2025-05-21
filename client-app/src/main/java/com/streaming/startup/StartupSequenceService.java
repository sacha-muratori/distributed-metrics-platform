package com.streaming.startup;

import com.streaming.client.registration.service.ClientRegistrationService;
import com.streaming.configuration.properties.service.ConfigurationPropertiesService;
import com.streaming.metrics.collector.scheduler.SystemMetricsCollectorScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupSequenceService {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private ClientRegistrationService clientRegistrationService;

    @Autowired
    private ConfigurationPropertiesService metricsConfigurationPropertiesService;

    @Autowired
    private SystemMetricsCollectorScheduler systemMetricsCollectorScheduler;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        // Fetch Client, Schedule and Strategies Configuration from Server
        metricsConfigurationPropertiesService.fetchAndUpdateMetricsConfigs();                   // logs warning if fails

        // Register Client as new device towards Server
        clientRegistrationService.registerClient();                                             // logs warning if fails

        // Start Scheduler Collectors regardless of what failed — we have fallbacks for registering and properties.
        systemMetricsCollectorScheduler.scheduleTasks();
    }
}
