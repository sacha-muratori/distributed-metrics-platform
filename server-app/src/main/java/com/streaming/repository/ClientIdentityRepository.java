package com.streaming.repository;

import com.streaming.repository.model.ClientIdentityDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ClientIdentityRepository extends ReactiveMongoRepository<ClientIdentityDocument, String> {
    Mono<ClientIdentityDocument> findByFingerprint(String fingerprint);
}
