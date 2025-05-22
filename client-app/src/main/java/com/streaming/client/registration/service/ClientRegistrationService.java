package com.streaming.client.registration.service;

import com.streaming.client.identity.helper.FingerprintGenerator;
import com.streaming.client.identity.model.ClientIdentity;
import com.streaming.client.identity.store.ClientIdentityStoreService;
import com.streaming.configuration.properties.model.ClientConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class ClientRegistrationService {

    @Autowired
    private ClientConfigurationProperties clientConfigurationProperties;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ClientIdentityStoreService clientStore;

    /**
     * Tries to register client if not registered yet.
     * Returns Mono<Boolean> indicating whether a registration was performed (true) or skipped (false).
     */
    public Mono<Boolean> registerClient() {
        ClientIdentity client = clientStore.load();

        if (client.getFingerprint() == null) {
            client.setFingerprint(FingerprintGenerator.generate());
            log.debug("Generated Client fingerprint: {}", client.getFingerprint());
            clientStore.save(client);
        }

        if (client.getClientId() != null) {
            // Already registered, skip
            return Mono.just(false);
        }

        log.debug("ClientId is null, attempting registration.");

        return webClient.post()
                .uri(clientConfigurationProperties.getRegistrationUrl())
                .bodyValue(Map.of("fingerprint", client.getFingerprint()))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String clientId = (String) response.get("clientId");
                    if (clientId != null) {
                        client.setClientId(clientId);
                        clientStore.save(client);
                        log.info("Successfully registered with clientId: {}", clientId);
                        return Mono.just(true);
                    } else {
                        log.warn("Registration succeeded but clientId missing");
                        return Mono.just(false);
                    }
                })
                .doOnError(e -> log.warn("Registration failed, will try next time: {}", e.getMessage()))
                .onErrorReturn(false);
    }
}
