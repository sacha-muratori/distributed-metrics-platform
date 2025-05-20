package com.streaming.metrics.dispatcher.spark.flusher;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String METRICS_DIR = "metrics"; // ensure this exists
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public void flushToDisk(List<Map<String, Object>> batch) {
        if (batch == null || batch.isEmpty()) return;

        String fileName = "metrics-" + FILE_FORMAT.format(ZonedDateTime.now()) + ".json";
        Path path = Paths.get(METRICS_DIR, fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            for (Map<String, Object> entry : batch) {
                writer.write(new ObjectMapper().writeValueAsString(entry));
                writer.newLine();
            }
        } catch (IOException e) {
            // In production, retry or enqueue to fallback handler
            e.printStackTrace();
        }
    }
}
