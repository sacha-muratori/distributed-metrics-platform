package com.streaming.metrics.collector.scheduler;

import com.streaming.configuration.properties.model.MetricsConfigurationProperties;
import com.streaming.configuration.properties.model.holder.ConfigurationPropertiesHolder;
import com.streaming.metrics.collector.scheduler.helper.ScheduledTaskHandler;
import com.streaming.metrics.collector.service.SystemMetricsCollectorService;
import com.streaming.metrics.dispatcher.aggregated.AggregatedMetricsDispatcherService;
import com.streaming.metrics.dispatcher.retry.RetryMetricsDispatcherService;
import com.streaming.metrics.dispatcher.spark.service.SparkMetricsDispatcherService;
import com.streaming.metrics.dispatcher.spark.store.SparkMetricsCollectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class SystemMetricsCollectorScheduler {

    @Autowired
    private SystemMetricsCollectorService systemMetricsCollectorService;

    @Autowired
    private SparkMetricsCollectorStoreService rawMetricsCollectorStoreService;

    @Autowired
    private SparkMetricsDispatcherService sparkMetricsDispatcherService;

    @Autowired
    private AggregatedMetricsDispatcherService aggregatedMetricsDispatcherService;

    @Autowired
    private RetryMetricsDispatcherService retryMetricsDispatcherService;

    @Autowired
    private ConfigurationPropertiesHolder configHolder;

    @Autowired
    private TaskScheduler taskScheduler;

    private ScheduledTaskHandler sparksTaskHandler;
    private ScheduledTaskHandler aggregatedTaskHandler;
    private ScheduledTaskHandler archivedRetryTaskHandler;

    public void rescheduleMetricsCollectionTasks() {
        log.info("Rescheduling metrics collection tasks due to config update");
        if (sparksTaskHandler != null) sparksTaskHandler.cancel();
        if (aggregatedTaskHandler != null) aggregatedTaskHandler.cancel();
        if (archivedRetryTaskHandler != null) archivedRetryTaskHandler.cancel();
        scheduleTasks();
    }

    public void scheduleTasks() {
        log.debug("Starting scheduled tasks");
        MetricsConfigurationProperties config = configHolder.getMetricsConfigRef();

        // SPARKS SCHEDULER - DEFAULT 1s
        MetricsConfigurationProperties.Spark spark = config.getCollector().getSpark();
        sparksTaskHandler = new ScheduledTaskHandler(
                taskScheduler,
                this::sparksMetricsCollectionTask,
                spark.getInitialDelayMs(),
                spark.getSchedulerIntervalMs()
        );

        // AGGREGATED SCHEDULER - DEFAULT 60s
        MetricsConfigurationProperties.Aggregated aggregated = config.getCollector().getAggregated();
        aggregatedTaskHandler = new ScheduledTaskHandler(
                taskScheduler,
                this::aggregatedMetricsCollectionTask,
                aggregated.getInitialDelayMs(),
                aggregated.getSchedulerIntervalMs()
        );

        // ARCHIVED RETRY SCHEDULER - DEFAULT 2m
        MetricsConfigurationProperties.Retry retry = config.getCollector().getRetry();
        archivedRetryTaskHandler = new ScheduledTaskHandler(
                taskScheduler,
                this::archivedMetricsRetryTask,
                retry.getInitialDelayMs(),
                retry.getSchedulerIntervalMs()
        );

        sparksTaskHandler.schedule();
        aggregatedTaskHandler.schedule();
        archivedRetryTaskHandler.schedule();

        log.info("Scheduled metrics collection tasks");
    }

    /** ACTUAL SCHEDULERS LOGIC */
    private void sparksMetricsCollectionTask() {
        log.info("Starting Metrics Collection - Sparks");
        Map<String, Object> metrics = systemMetricsCollectorService.collectMetrics();
        rawMetricsCollectorStoreService.appendMetric(metrics);

        double systemCpuUsagePercent = (double) metrics.get("systemCpuUsagePercent");
        double threshold = configHolder.getMetricsConfigRef().getCollector().getThreshold().getHighCpuPercentage();

        if (systemCpuUsagePercent >= threshold) {
            // Fire and Forget - Fault Tolerance is in Aggregated Metrics Collection Scheduler
            sparkMetricsDispatcherService.sendSparkAlert(metrics);
        }
    }

    private void aggregatedMetricsCollectionTask() {
        log.info("Starting Metrics Collection - Aggregated");
        // Resilient Dispatcher for avoiding losing data
        aggregatedMetricsDispatcherService.dispatchReadyMetrics();
    }

    private void archivedMetricsRetryTask() {
        log.info("Starting Archived Metrics Retry Task");
        // Retry Dispatcher for avoiding losing data
        retryMetricsDispatcherService.retryArchivedDispatches();
    }

}
