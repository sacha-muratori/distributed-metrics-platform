package com.streaming.repository;

import com.streaming.repository.model.MetricsDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Repository
public interface MetricsRepository extends ReactiveMongoRepository<MetricsDocument, String> {

    Flux<MetricsDocument> findByClientIdAndTimestampBetween(String clientId, Instant start, Instant end);

    Flux<MetricsDocument> findByTimestampBetween(Instant start, Instant end);

    Flux<MetricsDocument> findByClientId(String clientId);
}
