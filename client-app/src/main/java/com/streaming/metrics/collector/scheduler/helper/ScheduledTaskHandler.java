package com.streaming.metrics.collector.scheduler.helper;

import org.springframework.scheduling.TaskScheduler;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

public class ScheduledTaskHandler {

    private final TaskScheduler scheduler;
    private final Runnable task;
    private final long initialDelay;
    private final long fixedRate;
    private ScheduledFuture<?> future;

    public ScheduledTaskHandler(TaskScheduler scheduler, Runnable task, long initialDelay, long fixedRate) {
        this.scheduler = scheduler;
        this.task = task;
        this.initialDelay = initialDelay;
        this.fixedRate = fixedRate;
    }

    public void schedule() {
        cancel(); // Cancel previous if running

        long now = System.currentTimeMillis();
        future = scheduler.schedule(() -> {
            task.run();
            future = scheduler.scheduleAtFixedRate(task, fixedRate);
        }, new Date(now + initialDelay));
    }

    public void cancel() {
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }
}
