package com.streaming.metrics.collector.strategy.impl;

import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.HashMap;
import java.util.Map;

public class CpuMetricsCollectorStrategy implements MetricsCollectorStrategy {

    private final CentralProcessor processor;
    private long[] previousTicks;
    private boolean isFirstMeasurement;

    public CpuMetricsCollectorStrategy() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        this.processor = hal.getProcessor();
        this.previousTicks = processor.getSystemCpuLoadTicks();
        this.isFirstMeasurement = true;
    }

    @Override
    public String getName() {
        return MetricsCollectorStrategyType.CPU.getName();
    }

    @Override
    public Map<String, Object> collect() {
        double load;

        if (isFirstMeasurement) {
            // On first call, we can't calculate between ticks reliably
            load = 0.0;
            isFirstMeasurement = false;
        } else {
            load = processor.getSystemCpuLoadBetweenTicks(previousTicks) * 100;
        }
        previousTicks = processor.getSystemCpuLoadTicks(); // Update for next call

        Map<String, Object> result = new HashMap<>();
        result.put("availableProcessors", processor.getLogicalProcessorCount());
        result.put("systemCpuUsagePercent", load);
        return result;
    }
}
