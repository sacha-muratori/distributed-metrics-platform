package com.streaming.metrics.dispatcher.retry;

import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class RetryMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    private static final Path ARCHIVE_DIR = Paths.get("data/archive");

    @Autowired
    private WebClient webClient;

    @Autowired
    private ConfigurationPropertiesHolder configurationPropertiesHolder;

    public void retryArchivedDispatches() {
        log.debug("Retrying archived dispatches");

        if (!Files.exists(ARCHIVE_DIR)) {
            log.debug("Archive directory does not exist. Nothing to retry.");
            return;
        }

        try (Stream<Path> files = Files.list(ARCHIVE_DIR)) {
            files
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(this::attemptResend);
        } catch (IOException e) {
            log.error("Error listing archived files", e);
        }

        log.debug("Archived retry pass complete.");
    }

    private void attemptResend(Path file) {
        try {
            String url = configurationPropertiesHolder.getMetricsConfigRef().getCollector().getAggregated().getAggregatedUrl();
            byte[] data = Files.readAllBytes(file);

            webClient.post()
                    .uri(url)
                    .bodyValue(data)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(resp -> {
                        log.info("Resent archived file: {}", file.getFileName());
                        delete(file);
                    })
                    .doOnError(err -> {
                        if (err instanceof WebClientResponseException wce) {
                            log.warn("Failed to resend {}: HTTP {}", file.getFileName(), wce.getStatusCode());
                        } else {
                            log.warn("Failed to resend {}: {}", file.getFileName(), err.getMessage());
                        }
                    })
                    .subscribe();

        } catch (IOException e) {
            log.error("Could not read archived file: {}", file.getFileName(), e);
        }
    }

    private void delete(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Could not delete archived file: {}", file.getFileName(), e);
        }
    }
}
