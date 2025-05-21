package com.streaming.metrics.collector.strategy.helper;

import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.CpuMetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.DiskMetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.MemoryMetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.NetworkMetricsCollectorStrategy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class MetricsCollectorStrategyFactory {

    private static final Map<MetricsCollectorStrategyType, MetricsCollectorStrategy> CACHE = new EnumMap<>(MetricsCollectorStrategyType.class);

    public static Optional<MetricsCollectorStrategy> create(MetricsCollectorStrategyType type) {
        return Optional.ofNullable(CACHE.computeIfAbsent(type, t -> switch (t) {
            case CPU -> new CpuMetricsCollectorStrategy();
            case MEMORY -> new MemoryMetricsCollectorStrategy();
            case DISK -> new DiskMetricsCollectorStrategy();
            case NETWORK -> new NetworkMetricsCollectorStrategy();
        }));
    }
}