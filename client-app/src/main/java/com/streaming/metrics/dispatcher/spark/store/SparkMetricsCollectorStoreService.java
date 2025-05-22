package com.streaming.metrics.dispatcher.spark.store;

import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import com.streaming.metrics.dispatcher.spark.buffer.InMemorySparkMetricsBuffer;
import com.streaming.metrics.dispatcher.spark.flusher.SparkMetricsFlusherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SparkMetricsCollectorStoreService {

    private static final int FLUSH_THRESHOLD = 200; // Should be calculated dynamically based on the Strategies chosen
    // Considering 1 metric ≈ 1KB, want to dispatch a file with 200 metrics to not overload the server with POST requests.

    @Autowired
    private InMemorySparkMetricsBuffer buffer;

    @Autowired
    private ConfigurationPropertiesHolder configHolder;

    @Autowired
    private SparkMetricsFlusherService flusherService;


    public void appendMetric(Map<String, Object> metrics) {
        buffer.store(metrics);

        if (buffer.size() >= FLUSH_THRESHOLD) {
            List<Map<String, Object>> batch = buffer.drainBatch(FLUSH_THRESHOLD);

            flusherService.flushToDisk(batch);
        }
    }
}