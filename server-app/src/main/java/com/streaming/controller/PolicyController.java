package com.streaming.controller;

import com.streaming.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @PostMapping("/policy")
    public Mono<ResponseEntity<Void>> fetchMetricsConfiguration() {
        return Mono
            .flatMap(policyService::fetchMetricsConfiguration)
            .thenReturn(ResponseEntity.ok().build());
    }
}