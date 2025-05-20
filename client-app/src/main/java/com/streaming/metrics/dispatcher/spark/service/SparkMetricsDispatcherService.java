package com.streaming.metrics.dispatcher.spark.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class SparkMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    private final WebClient webClient;

    public SparkMetricsDispatcherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/metrics/").build();
    }

    public void sendSparkAlert(Map<String, Object> metrics) {
        log.debug("Dispatching raw alert for high CPU");

        webClient.post()
                .uri("/spark-alert")
                .bodyValue(metrics)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.debug("Spark alert sent successfully"))
                .doOnError(error -> log.warn("Failed to send spark alert", error))
                .subscribe(); // Fire-and-forget
    }
}
