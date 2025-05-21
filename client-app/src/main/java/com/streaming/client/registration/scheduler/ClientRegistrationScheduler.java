package com.streaming.client.registration.scheduler;

import com.streaming.client.registration.service.ClientRegistrationService;
import com.streaming.configuration.properties.model.ClientConfigurationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClientRegistrationScheduler {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private ClientConfigurationProperties clientConfigurationProperties;

    @Autowired
    private ClientRegistrationService registrationService;

    // Attemps to register the client and get the UUID from the Server if previously not done
    // Does not interact with I/O but with in-memory Client Identity cached object each load,
    // which immediately short-circuits the execution after a client has been registered once and for all.
    @Scheduled(
            fixedRateString = "#{@clientConfigurationProperties.schedulerIntervalMs}",
            initialDelayString = "#{@clientConfigurationProperties.initialDelayMs}"
    )
    public void attemptRegistrationIfUnregistered() {
        registrationService.registerClient();
    }
}
