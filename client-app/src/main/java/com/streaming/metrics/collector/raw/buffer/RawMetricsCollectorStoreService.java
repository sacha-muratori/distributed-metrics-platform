package com.streaming.metrics.collector.raw.buffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RawMetricsCollectorStoreService {

    private static final int FLUSH_THRESHOLD = 10;                                                                      // TODO to come from application.yml/configuration properties

    @Autowired
    private InMemoryRawMetricsBuffer buffer;

    @Autowired
    private RawMetricsFlusherService flusherService;

    public void appendMetric(Map<String, Object> metrics) {
        buffer.store(metrics);

        if (buffer.size() >= FLUSH_THRESHOLD) {
            List<Map<String, Object>> batch = buffer.drainBatch(FLUSH_THRESHOLD);
            flusherService.flushToDisk(batch);
        }
    }
}