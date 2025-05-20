package com.streaming.configuration.properties.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client")
@Data
public class ClientConfigurationProperties {

    private String registrationUrl;
    private long schedulerIntervalMs;
    private long initialDelayMs;
}