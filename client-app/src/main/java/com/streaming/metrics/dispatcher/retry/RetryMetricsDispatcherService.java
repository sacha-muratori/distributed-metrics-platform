package com.streaming.metrics.dispatcher.retry;

import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Service
public class RetryMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    private static final Path ARCHIVE_DIR = Paths.get("data/archive");
    private static final int MAX_FILES_PER_RUN = 100;

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
                    .limit(MAX_FILES_PER_RUN)
                    .forEach(this::attemptResend);
        } catch (IOException e) {
            log.error("Error listing archived files", e);
        }

        log.debug("Archived retry pass complete.");
    }

    private void attemptResend(Path file) {
        Path sendingFile = file.resolveSibling(file.getFileName().toString().replace(".json", ".sending"));
        try {
            // Attempt to move file to .sending — skip if someone else already is retrying it
            try {
                Files.move(file, sendingFile);
            } catch (FileAlreadyExistsException | AtomicMoveNotSupportedException e) {
                log.debug("File already being retried by another thread: {}", file.getFileName());
                return;
            }

            byte[] data = Files.readAllBytes(sendingFile);
            String url = configurationPropertiesHolder.getMetricsConfigRef().getCollector().getAggregated().getAggregatedUrl();

            webClient.post()
                    .uri(url)
                    .bodyValue(data)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(resp -> {
                        log.info("Successfully resent archived file: {}", sendingFile.getFileName());
                        delete(sendingFile);
                    })
                    .doOnError(err -> {
                        log.warn("Failed to resend {}: {}", sendingFile.getFileName(), err.getMessage());
                        // Revert file for next retry pass
                        try {
                            Files.move(sendingFile, file, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException moveBackErr) {
                            log.error("Could not restore file {} for retry", sendingFile.getFileName(), moveBackErr);
                        }
                    })
                    .subscribe();

        } catch (IOException e) {
            log.error("Could not read or move archived file: {}", file.getFileName(), e);
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
