package com.streaming.configuration.properties.model.holder;

import com.streaming.configuration.properties.model.MetricsConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class ConfigurationPropertiesHolder {

    private final AtomicReference<MetricsConfigurationProperties> metricsConfigRef = new AtomicReference<>();

    @Autowired
    public ConfigurationPropertiesHolder(MetricsConfigurationProperties metricsConfigurationProperties) {
        this.metricsConfigRef.set(metricsConfigurationProperties);
    }

    public MetricsConfigurationProperties getMetricsConfigRef() {
        return metricsConfigRef.get();
    }

    public void setMetricsConfigRef(MetricsConfigurationProperties metricsConfigurationProperties) {
        this.metricsConfigRef.set(metricsConfigurationProperties);
    }
}