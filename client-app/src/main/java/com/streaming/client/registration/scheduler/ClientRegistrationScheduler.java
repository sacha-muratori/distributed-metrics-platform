package com.streaming.client.registration.scheduler;

import com.streaming.client.identity.model.ClientIdentity;
import com.streaming.client.identity.store.ClientIdentityStoreService;
import com.streaming.client.registration.service.ClientRegistrationService;
import com.streaming.properties.model.MetricsConfigurationProperties;
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
    private MetricsConfigurationProperties metricsProperties;

    @Scheduled(fixedRateString = "#{@metricsProperties.client.registrationRetryMs}")
    public void attemptRegistrationIfUnregistered() {
        ClientIdentity client = clientStore.load();

        if (client.getClientId() == null) {
            log.info("ClientId is still null. Attempting background registration.");
            registrationService.tryRegisterClient(client);
            clientStore.save(client);
        }
    }
}
