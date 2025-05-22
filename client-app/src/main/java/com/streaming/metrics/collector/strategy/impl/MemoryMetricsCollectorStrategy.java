package com.streaming.metrics.collector.strategy.impl;

import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class MemoryMetricsCollectorStrategy implements MetricsCollectorStrategy {

    @Override
    public String getName() {
        return MetricsCollectorStrategyType.MEMORY.getName();
    }

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();

        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long totalPhysicalMemory = osBean.getTotalMemorySize();
        long freePhysicalMemory = osBean.getFreeMemorySize();
        long usedPhysicalMemory = totalPhysicalMemory - freePhysicalMemory;

        result.put("totalPhysicalMemoryBytes", totalPhysicalMemory);
        result.put("freePhysicalMemoryBytes", freePhysicalMemory);
        result.put("usedPhysicalMemoryBytes", usedPhysicalMemory);

        return result;
    }
}
