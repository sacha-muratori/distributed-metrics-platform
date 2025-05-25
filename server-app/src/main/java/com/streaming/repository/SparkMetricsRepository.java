package com.streaming.repository;

import com.streaming.repository.model.MetricsDocument;
import com.streaming.repository.model.SparkMetricsDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Repository
public interface SparkMetricsRepository extends ReactiveMongoRepository<SparkMetricsDocument, String> {

    // Find by clientId only
    Flux<SparkMetricsDocument> findByClientId(String clientId);

    // Find by timestamp between start and end (any client)
    Flux<SparkMetricsDocument> findByTimestampBetween(Instant start, Instant end);

    // Find by clientId and timestamp between start and end
    Flux<SparkMetricsDocument> findByClientIdAndTimestampBetween(String clientId, Instant start, Instant end);

    // Find by fingerprint and timestamp between
    Flux<SparkMetricsDocument> findByFingerprintAndTimestampBetween(String fingerprint, Instant start, Instant end);
}
