package com.streaming.metrics.dispatcher.aggregated;

import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class AggregatedMetricsDispatcherService {

    private final Logger log = LogManager.getLogger(getClass());

    private static final String METRICS_DIR = "data/metrics";
    private static final String ARCHIVE_DIR = "data/archive";

    @Autowired
    private WebClient webClient;

    @Autowired
    private ConfigurationPropertiesHolder configurationPropertiesHolder;

    public void dispatchReadyMetrics() {
        try (Stream<Path> readyFiles = Files.list(Paths.get(METRICS_DIR))
                .filter(path -> path.toString().endsWith(".ready"))) {

            readyFiles.forEach(this::processReadyFile);

        } catch (IOException e) {
            log.error("Failed to scan .ready files in {}", METRICS_DIR, e);
        }
    }

    private void processReadyFile(Path readyFile) {
        String baseName = readyFile.getFileName().toString().replace(".ready", "");
        Path jsonFile = Paths.get(METRICS_DIR, baseName + ".json");
        Path sendingFile = Paths.get(METRICS_DIR, baseName + ".sending");

        try {
            Files.move(readyFile, sendingFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("Could not rename .ready to .sending for file: {}", readyFile.getFileName(), e);
            return;
        }

        if (!Files.exists(jsonFile)) {
            log.warn("Missing .json for ready file: {}", jsonFile.getFileName());
            delete(sendingFile); // remove the renamed flag file
            return;
        }

        try {
            String url = configurationPropertiesHolder.getMetricsConfigRef().getCollector().getAggregated().getAggregatedUrl();
            byte[] data = Files.readAllBytes(jsonFile);

            webClient.post()
                    .uri(url)
                    .bodyValue(data)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response -> {
                        log.info("Successfully sent: {}", jsonFile.getFileName());
                        delete(jsonFile);
                        delete(sendingFile);
                    })
                    .onErrorResume(e -> {
                        log.warn("Failed to send: {}", jsonFile.getFileName(), e.getMessage());
                        moveToArchive(jsonFile);
                        delete(sendingFile); // we only need .json archived
                        return Mono.empty(); // emit nothing, avoid returning any value as it is a post request
                    })
                    .subscribe();

        } catch (IOException e) {
            log.warn("Error reading metrics file: {}", jsonFile.getFileName(), e);
            moveToArchive(jsonFile);
            delete(sendingFile);
        }
    }

    private void delete(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Could not delete file: {}", file.getFileName(), e);
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
            log.error("Failed to archive file: {}", file.getFileName(), e);
        }
    }
}
