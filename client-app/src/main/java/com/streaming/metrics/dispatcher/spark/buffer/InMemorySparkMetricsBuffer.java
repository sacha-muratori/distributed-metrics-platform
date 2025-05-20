package com.streaming.metrics.dispatcher.spark.buffer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InMemorySparkMetricsBuffer {

    private final Queue<Map<String, Object>> buffer = new ConcurrentLinkedQueue<>();

    public void store(Map<String, Object> metrics) {
        buffer.add(metrics);
    }

    public List<Map<String, Object>> drainBatch(int maxSize) {
        List<Map<String, Object>> batch = new ArrayList<>(maxSize);
        for (int i = 0; i < maxSize; i++) {
            Map<String, Object> entry = buffer.poll();
            if (entry == null) break;
            batch.add(entry);
        }
        return batch;
    }

    public int size() {
        return buffer.size();
    }
}
