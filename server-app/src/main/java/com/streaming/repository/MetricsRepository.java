package com.streaming.repository;

import com.streaming.repository.model.MetricsDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Repository
public interface MetricsRepository extends ReactiveMongoRepository<MetricsDocument, String> {

    // Find by clientId only
    Flux<MetricsDocument> findByClientId(String clientId);

    // Find by timestamp between start and end (any client)
    Flux<MetricsDocument> findByTimestampBetween(Instant start, Instant end);

    // Find by clientId and timestamp between start and end
    Flux<MetricsDocument> findByClientIdAndTimestampBetween(String clientId, Instant start, Instant end);

    // Find by fingerprint and timestamp between
    Flux<MetricsDocument> findByFingerprintAndTimestampBetween(String fingerprint, Instant start, Instant end);

}