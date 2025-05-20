package com.streaming.client.registration.scheduler;

import com.streaming.client.identity.model.ClientIdentity;
import com.streaming.client.identity.store.ClientIdentityStoreService;
import com.streaming.client.registration.service.ClientRegistrationService;
import com.streaming.configuration.properties.model.ClientConfigurationProperties;
import com.streaming.configuration.properties.model.MetricsConfigurationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ClientRegistrationScheduler {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private ClientIdentityStoreService clientStore;

    @Autowired
    private ClientRegistrationService registrationService;

    @Autowired
    private ClientConfigurationProperties clientConfigurationProperties;

    @Scheduled(
            fixedRateString = "#{@clientConfigurationProperties.schedulerIntervalMs}",
            initialDelayString = "#{@clientConfigurationProperties.initialDelayMs}"
    )
    public void attemptRegistrationIfUnregistered() {
        // Attemps to register the client and get the UUID from the Server if previously not done
        // Does not interact with I/O but with in-memory Client Identity cached object each load,
        // which immediately short-circuits the execution after a client has been registered once and for all.
        ClientIdentity client = clientStore.load();

        if (client.getClientId() == null) {
            log.info("ClientId is still null. Attempting background registration.");
            registrationService.tryRegisterClient(client);
            clientStore.save(client);
        }
    }
}
