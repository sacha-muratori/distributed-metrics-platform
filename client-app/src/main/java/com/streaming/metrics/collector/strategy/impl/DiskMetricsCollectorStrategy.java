package com.streaming.metrics.collector.strategy.impl;

import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DiskMetricsCollectorStrategy implements MetricsCollectorStrategy {

    @Override
    public String getName() {
        return MetricsCollectorStrategyType.DISK.getName();
    }

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();
        File root = new File("/"); // Can customize per mount point
        // Use    = File.listRoots() for the total of multiple roots (Windows has C:/, D:/ etc)

        result.put("usableDiskBytes", root.getUsableSpace());
        result.put("totalDiskBytes", root.getTotalSpace());
        result.put("freeDiskBytes", root.getFreeSpace());

        return result;
    }
}

