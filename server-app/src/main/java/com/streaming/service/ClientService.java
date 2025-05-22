package com.streaming.service;

import com.streaming.repository.ClientIdentityRepository;
import com.streaming.repository.model.ClientIdentityDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class ClientService {

    @Autowired
    private ClientIdentityRepository repository;

    public Mono<Map<String, Object>> registerClient(Map<String, Object> requestBody) {
        String fingerprint = (String) requestBody.get("fingerprint");
        if (fingerprint == null || fingerprint.isBlank()) {
            return Mono.error(new IllegalArgumentException("Missing fingerprint in request"));
        }

        return repository.findByFingerprint(fingerprint)
                .switchIfEmpty(
                        repository.save(createClientIdentity(fingerprint))
                )
                .map(clientIdentity -> Map.of("clientId", clientIdentity.getClientId()));
    }

    private ClientIdentityDocument createClientIdentity(String fingerprint) {
        String clientId = UUID.randomUUID().toString();
        return new ClientIdentityDocument(clientId, fingerprint);
    }
}
