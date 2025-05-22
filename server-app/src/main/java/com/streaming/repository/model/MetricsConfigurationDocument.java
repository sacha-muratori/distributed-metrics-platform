package com.streaming.repository.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "metrics_policy")
@Data
public class MetricsConfigurationDocument {

    @Id
    private int id;

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
        private double highCpuPercentage;
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
