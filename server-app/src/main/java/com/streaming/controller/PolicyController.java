package com.streaming.controller;

import com.streaming.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @GetMapping("/policy")
    public Mono<ResponseEntity<Map<String, Object>>> fetchMetricsConfiguration() {
        return policyService.fetchMetricsConfiguration().map(ResponseEntity::ok);
    }
}
