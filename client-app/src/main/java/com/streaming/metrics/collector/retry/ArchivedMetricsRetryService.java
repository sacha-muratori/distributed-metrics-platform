package com.streaming.metrics.collector.retry;

import com.streaming.metrics.collector.aggregated.dispatcher.AggregatedMetricsDispatcherService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Service
public class ArchivedMetricsRetryService {

    private final Logger log = LogManager.getLogger(getClass());

    private static final Path ARCHIVE_DIR = Paths.get("archive");

    private final AggregatedMetricsDispatcherService dispatcher;

    public ArchivedMetricsRetryService(AggregatedMetricsDispatcherService dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void retryArchivedDispatches() {
        log.debug("Attempting to retry dispatching archived metrics files...");

        if (!Files.exists(ARCHIVE_DIR)) {
            log.info("Archive directory does not exist. No archived files to retry.");
            return;
        }

        try (Stream<Path> files = Files.list(ARCHIVE_DIR)) {
            files
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(this::attemptResend);
        } catch (IOException e) {
            log.error("Error while listing archived files", e);
        }

        log.debug("Archived dispatch retry completed");
    }

    private void attemptResend(Path file) {
        try {
            byte[] data = Files.readAllBytes(file);

            dispatcher.getWebClient()
                    .post()
                    .uri("/aggregated-metrics")
                    .bodyValue(data)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(resp -> {
                        log.info("Successfully resent: {}", file.getFileName());
                        delete(file);
                    })
                    .doOnError(err -> {
                        if (err instanceof WebClientResponseException) {
                            log.warn("Failed to resend {}: {}", file.getFileName(), ((WebClientResponseException) err).getStatusCode());
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
            Files.delete(file);
        } catch (IOException e) {
            log.error("Could not delete file after successful resend: {}", file.getFileName(), e);
        }
    }
}
