package com.streaming.metrics.dispatcher.spark.flusher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SparkMetricsFlusherService {

    private final Logger log = LogManager.getLogger(getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Path METRICS_DIR = Paths.get("data/metrics");
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX");

    public SparkMetricsFlusherService() {
        ensureDirectoryExists();
    }

    public void flushToDisk(List<Map<String, Object>> batch) {
        if (batch == null || batch.isEmpty()) return;

        String timestamp = FILE_FORMAT.format(ZonedDateTime.now());
        String baseName = "metrics-" + timestamp;
        Path dataFile = METRICS_DIR.resolve(baseName + ".json");
        Path readyFile = METRICS_DIR.resolve(baseName + ".ready");

        try (BufferedWriter writer = Files.newBufferedWriter(dataFile, StandardOpenOption.CREATE_NEW)) {
            for (Map<String, Object> entry : batch) {
                writer.write(OBJECT_MAPPER.writeValueAsString(entry));
                writer.newLine();
            }
            // Write marker file
            Files.createFile(readyFile);

            log.debug("Flushed {} metrics to {} and wrote done marker", batch.size(), dataFile);
        } catch (IOException e) {
            log.error("Failed to flush metrics to {}: {}", dataFile, e.getMessage());
        }
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(METRICS_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create metrics directory: " + METRICS_DIR, e);
        }
    }
}
