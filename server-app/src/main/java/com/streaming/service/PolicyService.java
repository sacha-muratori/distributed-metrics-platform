package com.streaming.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.repository.MetricsPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class PolicyService {

    @Autowired
    private MetricsPolicyRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    public Mono<Map<String, Object>> fetchMetricsConfiguration() {
        return repository.findById("metrics_policy")
                .map(policy -> objectMapper.convertValue(policy, new TypeReference<>() {}));
    }
}
