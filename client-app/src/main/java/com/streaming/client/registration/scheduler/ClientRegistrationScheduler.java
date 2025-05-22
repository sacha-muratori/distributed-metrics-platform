package com.streaming.client.registration.scheduler;

import com.streaming.client.registration.service.ClientRegistrationService;
import com.streaming.configuration.properties.model.ClientConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClientRegistrationScheduler {

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
        registrationService.registerClient()
                .doOnNext(updated -> {
                    if (updated) {
                        log.info("Client registration updated.");
                    } else {
                        log.debug("Client already registered, no update needed.");
                    }
                })
                .doOnError(e -> log.warn("Error during client registration: {}", e.getMessage()))
                .subscribe();
    }
}