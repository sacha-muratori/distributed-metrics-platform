package com.streaming.metrics.dispatcher.aggregated;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AggregatedMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    private static final String METRICS_DIR = "metrics";
    private static final String ARCHIVE_DIR = "archive";
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final WebClient webClient;

    public AggregatedMetricsDispatcherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/metrics/").build();
    }

    public void dispatchLastMinuteMetrics() {
        String targetFileName = "metrics-" + FILE_FORMAT.format(ZonedDateTime.now().minusMinutes(1)) + ".json";
        Path metricsPath = Paths.get(METRICS_DIR, targetFileName);

        if (!Files.exists(metricsPath)) {
            log.warn("No file found for previous minute: {}", targetFileName);
            return;
        }

        try {
            byte[] data = Files.readAllBytes(metricsPath);

            webClient.post()
                    .uri("/aggregated-metrics")
                    .bodyValue(data)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response -> {
                        log.info("Sent: {}", targetFileName);
                        delete(metricsPath);
                    })
                    .doOnError(err -> {
                        log.error("Failed to send: {}", targetFileName, err);
                        moveToArchive(metricsPath);
                    })
                    .subscribe();

        } catch (IOException e) {
            log.error("Error reading file: {}", targetFileName, e);
        }
    }

    private void delete(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            log.error("Could not delete sent file: {}", file.getFileName(), e);
        }
    }

    private void moveToArchive(Path file) {
        try {
            Path archiveDir = Paths.get(ARCHIVE_DIR);
            if (!Files.exists(archiveDir)) {
                Files.createDirectories(archiveDir);
            }
            Files.move(file, archiveDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to move file to archive: {}", file.getFileName(), e);
        }
    }

    public WebClient getWebClient() {
        return webClient;
    }
}
