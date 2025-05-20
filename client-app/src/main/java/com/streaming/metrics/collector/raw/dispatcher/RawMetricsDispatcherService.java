package com.streaming.metrics.collector.raw.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class RawMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    private final WebClient webClient;

    public RawMetricsDispatcherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/metrics/").build();
    }

    public void sendRawAlert(Map<String, Object> metrics) {
        log.debug("Dispatching raw alert for high CPU");

        webClient.post()
                .uri("/raw-alert")
                .bodyValue(metrics)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.debug("Raw alert sent successfully"))
                .doOnError(error -> log.warn("Failed to send raw alert", error))
                .subscribe(); // Fire-and-forget
    }
}
