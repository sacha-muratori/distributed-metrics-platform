package com.streaming.configuration.properties.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "policy")
@Data
public class PolicyConfigurationProperties {

    private String fetchUrl;
    private long schedulerIntervalMs;
    private long initialDelayMs;
}