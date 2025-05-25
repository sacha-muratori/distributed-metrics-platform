package com.streaming.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.repository.MetricsPolicyRepository;
import com.streaming.repository.model.MetricsConfigurationDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PolicyService {

    @Autowired
    private MetricsPolicyRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    public Mono<MetricsConfigurationDocument> fetchMetricsConfiguration() {
        return repository.findById("metrics_policy")
                .map(policy -> objectMapper.convertValue(policy, new TypeReference<MetricsConfigurationDocument>() {}))
                .doOnNext(config -> log.debug("Successfully loaded metrics configuration: {}", config));
    }
}
