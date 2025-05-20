package com.streaming.metrics.collector.strategy.helper;

import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.CpuMetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.DiskMetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.MemoryMetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.impl.NetworkMetricsCollectorStrategy;

import java.util.Optional;

public class MetricsCollectorStrategyFactory {

    public static Optional<MetricsCollectorStrategy> create(MetricsCollectorStrategyType type) {
        return switch (type) {
            case CPU -> Optional.of(new CpuMetricsCollectorStrategy());
            case MEMORY -> Optional.of(new MemoryMetricsCollectorStrategy());
            case DISK -> Optional.of(new DiskMetricsCollectorStrategy());
            case NETWORK -> Optional.of(new NetworkMetricsCollectorStrategy());
        };
    }
}