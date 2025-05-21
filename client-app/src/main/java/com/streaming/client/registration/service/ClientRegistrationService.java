package com.streaming.client.registration.service;

import com.streaming.client.identity.helper.FingerprintGenerator;
import com.streaming.client.identity.model.ClientIdentity;
import com.streaming.client.identity.store.ClientIdentityStoreService;
import com.streaming.configuration.properties.model.ClientConfigurationProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ClientRegistrationService {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private ClientConfigurationProperties clientConfigurationProperties;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ClientIdentityStoreService clientStore;

    public void registerClient() {
        ClientIdentity client = clientStore.load();

        if (client.getFingerprint() == null) {
            client.setFingerprint(FingerprintGenerator.generate());
            log.debug("Generated Client fingerprint: {}", client.getFingerprint());
            clientStore.save(client);
        }

        if (client.getClientId() == null) {
            log.debug("ClientId is still null. Attempting background registration.");
            tryRegisterClient(client);
            clientStore.save(client);
        }
    }

    public void tryRegisterClient(ClientIdentity client) {
        // Don't register again if already registered
        if (client.getClientId() != null) {
            return;
        }

        log.debug("Attempting registration via {}", clientConfigurationProperties.getRegistrationUrl());

        webClient.post()
                .uri(clientConfigurationProperties.getRegistrationUrl())
                .bodyValue(Map.of("fingerprint", client.getFingerprint()))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> log.debug("Registration response: {}", response))
                .map(response -> {
                    String clientId = (String) response.get("clientId");
                    if (clientId != null) {
                        client.setClientId(clientId);
                        clientStore.save(client);

                        log.info("Successfully registered with clientId: {}", client.getClientId());
                    } else {
                        log.warn("Missing clientId in response, registration completed with fingerprint, retry later");
                    }
                    return null; // continue with Mono<Void>
                })
                .onErrorResume(e -> {
                    log.warn("Registration failed: {}", e.getMessage());
                    return Mono.empty(); // emit nothing, avoid returning any value
                })
                .block(); // returns null (Mono<Void>.block())

    }

}
