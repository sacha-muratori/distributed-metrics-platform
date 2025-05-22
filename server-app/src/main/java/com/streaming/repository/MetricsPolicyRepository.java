package com.streaming.repository;

import com.streaming.repository.model.MetricsConfigurationDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricsPolicyRepository extends ReactiveMongoRepository<MetricsConfigurationDocument, String> {
}