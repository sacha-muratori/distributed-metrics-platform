package com.streaming.properties.model;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class MetricsConfigurationPropertiesHolder {

    private final AtomicReference<MetricsConfigurationProperties> configRef;

    public MetricsConfigurationPropertiesHolder(MetricsConfigurationProperties initialConfig) {
        this.configRef = new AtomicReference<>(initialConfig);
    }

    public MetricsConfigurationProperties getConfig() {
        return configRef.get();
    }

    public void updateConfig(MetricsConfigurationProperties newConfig) {
        configRef.set(newConfig);
    }
}