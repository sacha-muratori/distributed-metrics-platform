package com.streaming.metrics.scheduler;

import com.streaming.metrics.collector.retry.ArchivedMetricsRetryService;
import com.streaming.metrics.collector.service.SystemMetricsCollectorService;
import com.streaming.metrics.collector.raw.buffer.RawMetricsCollectorStoreService;
import com.streaming.metrics.collector.aggregated.dispatcher.AggregatedMetricsDispatcherService;
import com.streaming.metrics.collector.raw.dispatcher.RawMetricsDispatcherService;
import com.streaming.metrics.scheduler.helper.ScheduledTaskHandler;
import com.streaming.properties.model.MetricsConfigurationProperties;
import com.streaming.properties.model.MetricsConfigurationPropertiesHolder;
import com.streaming.startup.event.AppReadyForCollectionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SystemMetricsCollectorScheduler {

    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    private SystemMetricsCollectorService systemMetricsCollectorService;

    @Autowired
    private RawMetricsCollectorStoreService rawMetricsCollectorStoreService;

    @Autowired
    private RawMetricsDispatcherService rawMetricsDispatcherService;

    @Autowired
    private AggregatedMetricsDispatcherService aggregatedMetricsDispatcherService;

    @Autowired
    private ArchivedMetricsRetryService archivedMetricsRetryService;

    @Autowired
    private MetricsConfigurationPropertiesHolder configHolder;

    @Autowired
    private TaskScheduler taskScheduler;

    private ScheduledTaskHandler sparksTaskHandler;
    private ScheduledTaskHandler aggregatedTaskHandler;
    private ScheduledTaskHandler archivedRetryTaskHandler;

    @EventListener(AppReadyForCollectionEvent.class)
    public void onAppReadyForCollection() {
        log.debug("AppReadyForCollectionEvent received, starting scheduled tasks...");
        scheduleTasks();
    }

    public void rescheduleIfNeeded() {
        log.info("Rescheduling metrics collection tasks due to config update");
        if (sparksTaskHandler != null) sparksTaskHandler.cancel();
        if (aggregatedTaskHandler != null) aggregatedTaskHandler.cancel();
        if (archivedRetryTaskHandler != null) archivedRetryTaskHandler.cancel();
        scheduleTasks();
    }

    private void scheduleTasks() {
        MetricsConfigurationProperties config = configHolder.getConfig();
        long initialDelay = config.getSchedule().getInitialDelayMs();
        long sparksRate = config.getSchedule().getSparksRateMs();
        long fixedRate = config.getSchedule().getFixedRateMs();

        // SPARKS SCHEDULER - DEFAULT 1s
        sparksTaskHandler = new ScheduledTaskHandler(
                taskScheduler,
                this::sparksMetricsCollectionTask,
                initialDelay,
                sparksRate
        );

        // AGGREGATED SCHEDULER - DEFAULT 60s
        aggregatedTaskHandler = new ScheduledTaskHandler(
                taskScheduler,
                this::aggregatedMetricsCollectionTask,
                initialDelay + 10_000, // small offset
                fixedRate
        );

        // ARCHIVED RETRY SCHEDULER (e.g., every 2 min)
        archivedRetryTaskHandler = new ScheduledTaskHandler(
                taskScheduler,
                this::archivedMetricsRetryTask,
                initialDelay + 10_000, // small offset              // TODO: to be parametrized
                120_000 // every 2 minutes                                    // TODO: to be parametrized
        );

        sparksTaskHandler.schedule();
        aggregatedTaskHandler.schedule();
        archivedRetryTaskHandler.schedule();

        log.info("Scheduled metrics collection tasks with initialDelay={}ms, sparksRate={}ms, fixedRate={}ms",
                initialDelay, sparksRate, fixedRate);
    }

    /** ACTUAL SCHEDULERS LOGIC */
    private void sparksMetricsCollectionTask() {
        log.debug("Starting Metrics Collection - RAW");
        Map<String, Object> metrics = systemMetricsCollectorService.collectMetrics();
        rawMetricsCollectorStoreService.appendMetric(metrics);

        String systemCpuUsagePercent = (String) metrics.get("systemCpuUsagePercent");
        int threshold = configHolder.getConfig().getThreshold().getHighCpuPercentage();

        if (Integer.parseInt(systemCpuUsagePercent) > threshold) {
            // Fire and Forget - Fault Tolerance is in Aggregated Metrics Collection Scheduler
            rawMetricsDispatcherService.sendRawAlert(metrics);
        }
    }

    private void aggregatedMetricsCollectionTask() {
        log.debug("Starting Metrics Collection - Aggregated");
        // Resilient Dispatcher for avoiding losing data
        aggregatedMetricsDispatcherService.dispatchLastMinuteMetrics();
    }

    private void archivedMetricsRetryTask() {
        log.debug("Starting Archived Metrics Retry Task");
        archivedMetricsRetryService.retryArchivedDispatches();
    }

}
