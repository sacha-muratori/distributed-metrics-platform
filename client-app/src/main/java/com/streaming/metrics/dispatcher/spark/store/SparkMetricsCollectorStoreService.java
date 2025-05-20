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

    @Autowired
    private InMemorySparkMetricsBuffer buffer;

    @Autowired
    private ConfigurationPropertiesHolder configHolder;

    @Autowired
    private SparkMetricsFlusherService flusherService;

    public void appendMetric(Map<String, Object> metrics) {
        buffer.store(metrics);

        int flushThreshold = calculateFlushThreshold();
        if (buffer.size() >= flushThreshold) {
            List<Map<String, Object>> batch = buffer.drainBatch(flushThreshold);

            flusherService.flushToDisk(batch);
        }
    }
    private int calculateFlushThreshold() {
        long aggregatedIntervalMs = configHolder.getMetricsConfigRef().getCollector()
                .getAggregated().getSchedulerIntervalMs();
        long sparkIntervalMs = configHolder.getMetricsConfigRef().getCollector()
                .getSpark().getSchedulerIntervalMs();

        if (sparkIntervalMs <= 0) {
            throw new IllegalArgumentException("sparkIntervalMs must be > 0");
        }

        // Flush every 10% of the number of spark intervals per aggregated interval
        int flushThreshold = Math.max(1, (int) (aggregatedIntervalMs / sparkIntervalMs) / 10);
        return flushThreshold;
    }
}