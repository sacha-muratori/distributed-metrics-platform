package com.streaming.metrics.collector.strategy.impl;

import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import com.sun.management.OperatingSystemMXBean;

@Component
public class CpuMetricsCollectorStrategy implements MetricsCollectorStrategy {

    private final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final int availableProcessors = Runtime.getRuntime().availableProcessors();

    @Override
    public String getName() {
        return MetricsCollectorStrategyType.CPU.getName();
    }

    @Override
    public Map<String, Object> collect() {
        double systemCpuLoad = osBean.getCpuLoad();            // [0.0 - 1.0] or -1 if not available
        double processCpuLoad = osBean.getProcessCpuLoad();    // [0.0 - 1.0] or -1 if not available

        Map<String, Object> result = new HashMap<>();
        result.put("availableProcessors", availableProcessors);
        result.put("systemCpuUsagePercent", systemCpuLoad >= 0 ? String.format("%.2f", systemCpuLoad * 100) : "N/A");
        result.put("jvmProcessCpuUsagePercent", processCpuLoad >= 0 ? String.format("%.2f", processCpuLoad * 100) : "N/A");
        return result;
    }
}
