package com.streaming.metrics.dispatcher.spark.service;

import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class SparkMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private WebClient webClient;

    @Autowired
    private ConfigurationPropertiesHolder configurationPropertiesHolder;


    public void sendSparkAlert(Map<String, Object> metrics) {
        log.debug("Dispatching raw alert for high CPU");

        String url = configurationPropertiesHolder.getMetricsConfigRef().getCollector().getSpark().getSparkAlertUrl();
        webClient.post()
                .uri(url)
                .bodyValue(metrics)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.debug("Spark alert sent successfully"))
                .doOnError(error -> log.warn("Failed to send spark alert", error))
                .subscribe(); // Fire-and-forget
    }
}
