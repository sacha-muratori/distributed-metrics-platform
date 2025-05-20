package com.streaming.client.registration.service;

import com.streaming.client.identity.model.ClientIdentity;
import com.streaming.client.identity.store.ClientIdentityStoreService;
import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import com.streaming.client.identity.helper.FingerprintGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ClientRegistrationService {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private ConfigurationPropertiesHolder configHolder;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClientIdentityStoreService clientStore;

    public void registerClient() {
        ClientIdentity client = clientStore.load();

        if (client.getFingerprint() == null) {
            client.setFingerprint(FingerprintGenerator.generate());
            log.debug("Generated fingerprint: {}", client.getFingerprint());
        }

        if (client.getClientId() == null) {
            tryRegisterClient(client);
        }

        clientStore.save(client);
    }

    public void tryRegisterClient(ClientIdentity client) {
        String url = configHolder.getClientConfigRef().getRegistrationUrl();
        log.debug("Attempting registration via {}", url);

        try {
            Map<?, ?> response = restTemplate.postForObject(
                    url,
                    Map.of("fingerprint", client.getFingerprint()),
                    Map.class
            );

            if (response != null && response.containsKey("clientId")) {
                client.setClientId(response.get("clientId").toString());
                log.info("Successfully registered with clientId: {}", client.getClientId());
            } else {
                log.warn("Missing clientId in response");
            }
        } catch (Exception e) {
            log.warn("Registration failed: {}", e.getMessage());
        }
    }

    public String getClientIdentifier() {
        ClientIdentity client = clientStore.load();
        return client.getClientId() != null ? client.getClientId() : client.getFingerprint();
    }
}
