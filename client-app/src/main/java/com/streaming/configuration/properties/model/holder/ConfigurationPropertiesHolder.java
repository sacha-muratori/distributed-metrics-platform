package com.streaming.configuration.properties.model.holder;

import com.streaming.configuration.properties.model.MetricsConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class ConfigurationPropertiesHolder {

    private final AtomicReference<MetricsConfigurationProperties> metricsConfigRef;

    public ConfigurationPropertiesHolder(AtomicReference<MetricsConfigurationProperties> metricsConfigRef) {
        this.metricsConfigRef = metricsConfigRef;
    }

    public MetricsConfigurationProperties getMetricsConfigRef() {
        return metricsConfigRef.get();
    }

    public void setMetricsConfigRef(MetricsConfigurationProperties metricsConfigurationProperties) {
        this.metricsConfigRef.set(metricsConfigurationProperties);
    }
}