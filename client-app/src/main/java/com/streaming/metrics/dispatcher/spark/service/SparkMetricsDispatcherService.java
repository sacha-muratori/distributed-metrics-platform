package com.streaming.metrics.dispatcher.spark.service;

import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;

@Service
public class SparkMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private ConfigurationPropertiesHolder configurationPropertiesHolder;

    // This WebClient is configured with a timeout of 1 seconds
    private final WebClient timeoutWebClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(1))))
            .build();

    public void sendSparkAlert(Map<String, Object> metrics) {
        log.debug("Dispatching raw alert for high CPU");

        String url = configurationPropertiesHolder.getMetricsConfigRef().getCollector().getSpark().getSparkAlertUrl();
        timeoutWebClient.post()
                .uri(url)
                .bodyValue(metrics)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.debug("Spark alert sent successfully"))
                .onErrorResume(e -> {
                    log.warn("Failed to send spark alert", e.getMessage());
                    return Mono.empty(); // emit nothing, avoid returning any value as it is a post request
                })
                .subscribe(); // Fire-and-forget
    }
}
