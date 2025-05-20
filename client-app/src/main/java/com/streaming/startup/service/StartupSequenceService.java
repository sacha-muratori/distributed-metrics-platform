package com.streaming.startup.service;

import com.streaming.client.registration.service.ClientRegistrationService;
import com.streaming.properties.service.MetricsConfigurationPropertiesService;
import com.streaming.startup.event.AppReadyForCollectionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupSequenceService {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ClientRegistrationService registrationService;

    @Autowired
    private MetricsConfigurationPropertiesService configService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        // Fetch Client, Schedule and Strategies Configuration from Server
        fetchAndUpdateConfig();

        // Register Client as new device towards Server
        registerClient();

        // Publish event to Start Scheduler Collectors regardless of what failed — we have fallbacks
        publisher.publishEvent(new AppReadyForCollectionEvent(this));
    }

    private void registerClient() {
        try {
            registrationService.registerClient(); // logs warning if fails
        } catch (Exception e) {
            log.warn("Client registration failed: {}", e.getMessage());
        }
    }

    private void fetchAndUpdateConfig() {
        try {
            configService.fetchAndUpdateConfig(); // logs warning if fails
        } catch (Exception e) {
            log.warn("Config fetch failed: {}", e.getMessage());
        }
    }
}
