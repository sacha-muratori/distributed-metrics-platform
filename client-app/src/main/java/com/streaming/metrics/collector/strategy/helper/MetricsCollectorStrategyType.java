package com.streaming.metrics.collector.strategy.helper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum MetricsCollectorStrategyType {
    CPU("cpu"),
    MEMORY("memory"),
    DISK("disk"),
    NETWORK("network");

    private final String name;
    MetricsCollectorStrategyType(String name) { this.name = name; }
    public String getName() { return name; }

    public static final List<String> ALL_STRATEGY_NAMES = Arrays.stream(values())
            .map(MetricsCollectorStrategyType::getName)
            .toList();

    public static Optional<MetricsCollectorStrategyType> fromName(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name.equalsIgnoreCase(name))
                .findFirst();
    }
}