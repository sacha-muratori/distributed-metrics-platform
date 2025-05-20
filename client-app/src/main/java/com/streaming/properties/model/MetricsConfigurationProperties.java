package com.streaming.properties.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "metrics")
@Data
public class MetricsConfigurationProperties {

    // TODO: if they don't get from the GET request need to check if they come from the appliaction/yml
    //  otherwise I need to directly put the values from that file in this constructor

    private List<String> enabledStrategies;
    private Schedule schedule = new Schedule();
    private Threshold threshold = new Threshold();
    private Client client = new Client();

    @Data
    public static class Schedule {
        private long initialDelayMs;
        private long sparksRateMs;
        private long fixedRateMs;
    }

    @Data
    public static class Threshold {
        private int highCpuPercentage;
        private String configFetchUrl;
        private long fetchIntervalMs;
    }

    @Data
    public static class Client {
        private String registrationUrl;
        private long refreshIntervalMs;
    }
}