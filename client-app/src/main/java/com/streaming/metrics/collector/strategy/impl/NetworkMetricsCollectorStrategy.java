package com.streaming.metrics.collector.strategy.impl;

import com.streaming.metrics.collector.strategy.contract.MetricsCollectorStrategy;
import com.streaming.metrics.collector.strategy.helper.MetricsCollectorStrategyType;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class NetworkMetricsCollectorStrategy implements MetricsCollectorStrategy {

    @Override
    public String getName() {
        return MetricsCollectorStrategyType.NETWORK.getName();
    }

    @Override
    public Map<String, Object> collect() {
        Map<String, Object> result = new HashMap<>();
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            result.put("hostname", localHost.getHostName());
            result.put("ipaddress", localHost.getHostAddress());
        } catch (Exception e) {
            result.put("hostname", "unknown");
            result.put("ipaddress", "unknown");
        }
        return result;
    }


}
