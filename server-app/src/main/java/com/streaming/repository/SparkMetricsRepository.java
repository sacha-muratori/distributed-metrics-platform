package com.streaming.repository;

import com.streaming.repository.model.SparkMetricsDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

@Repository
public interface SparkMetricsRepository extends ReactiveMongoRepository<SparkMetricsDocument, String> {

    Flux<SparkMetricsDocument> findByClientIdAndTimestampBetween(String clientId, Instant start, Instant end);

    Flux<SparkMetricsDocument> findByTimestampBetween(Instant start, Instant end);

    Flux<SparkMetricsDocument> findByClientId(String clientId);
}
