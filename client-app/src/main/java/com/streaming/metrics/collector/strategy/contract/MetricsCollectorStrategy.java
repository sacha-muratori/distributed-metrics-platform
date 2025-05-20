package com.streaming.metrics.collector.strategy.contract;

import java.util.Map;

public interface MetricsCollectorStrategy {
    String getName(); // e.g. "cpu", "memory", "hostname"
    Map<String, Object> collect();
}
