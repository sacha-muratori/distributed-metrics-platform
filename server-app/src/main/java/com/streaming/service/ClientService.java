package com.streaming.service;

import com.streaming.repository.ClientIdentityRepository;
import com.streaming.repository.model.ClientIdentityDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ClientService {

    @Autowired
    private ClientIdentityRepository repository;

    public Mono<Map<String, Object>> registerClient(Map<String, Object> requestBody) {
        String fingerprint = (String) requestBody.get("fingerprint");
        log.debug("Attempting registering client with fingerprint: ", fingerprint);

        if (fingerprint == null || fingerprint.isBlank()) {
            return Mono.error(new IllegalArgumentException("Missing fingerprint in request"));
        }

        return repository.findByFingerprint(fingerprint)
                .flatMap(existing -> {
                    String clientId = existing.getClientId();
                    if (clientId == null || clientId.isBlank()) {
                        // Overwrite the existing document with a new clientId
                        ClientIdentityDocument updated = createClientIdentity(fingerprint);
                        return repository.save(updated);
                    }
                    return Mono.just(existing);
                })
                .switchIfEmpty(
                        // If no document was found at all, create and save a new one
                        repository.save(createClientIdentity(fingerprint))
                )
                .doOnNext(clientIdentity ->
                        log.debug("Client identity resolved for fingerprint {}: {}", fingerprint, clientIdentity.getClientId())
                )
                .map(clientIdentity -> Map.of("clientId", clientIdentity.getClientId()));
    }

    private ClientIdentityDocument createClientIdentity(String fingerprint) {
        return new ClientIdentityDocument(UUID.randomUUID().toString(), fingerprint);
    }
}
