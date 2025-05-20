package com.streaming.configuration.properties.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "metrics")
@Data
public class MetricsConfigurationProperties {

    private Collector collector = new Collector();

    @Data
    public static class Collector {
        private List<String> enabledStrategies;
        private Threshold threshold = new Threshold();
        private Spark spark = new Spark();
        private Aggregated aggregated = new Aggregated();
        private Retry retry = new Retry();
    }

    @Data
    public static class Threshold {
        private int highCpuPercentage;
    }

    @Data
    public static class Spark {
        private String sparkAlertUrl;
        private long schedulerIntervalMs;
        private long initialDelayMs;
    }

    @Data
    public static class Aggregated {
        private String aggregatedUrl;
        private long schedulerIntervalMs;
        private long initialDelayMs;
    }

    @Data
    public static class Retry {
        private long schedulerIntervalMs;
        private long initialDelayMs;
    }
}
