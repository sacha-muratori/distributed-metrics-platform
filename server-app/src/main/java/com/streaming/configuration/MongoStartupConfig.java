package com.streaming.configuration;

import com.streaming.repository.MetricsPolicyRepository;
import com.streaming.repository.model.MetricsConfigurationDocument;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class MongoStartupConfig {

    @Bean
    CommandLineRunner init(MetricsPolicyRepository repository) {
        return args -> {
            MetricsConfigurationDocument entity = createDefaultMetricsConfiguration();

            // Insert only if it doesn't exist
            repository.findById("metrics_policy")
                    .switchIfEmpty(repository.save(entity))
                    .subscribe();
        };
    }

    private MetricsConfigurationDocument createDefaultMetricsConfiguration() {
        MetricsConfigurationDocument entity = new MetricsConfigurationDocument();
        entity.setId(1); // Assuming a static ID

        MetricsConfigurationDocument.Collector collector = new MetricsConfigurationDocument.Collector();

        // Enabled strategies
        collector.setEnabledStrategies(Collections.singletonList("cpu"));

        // Threshold
        MetricsConfigurationDocument.Threshold threshold = new MetricsConfigurationDocument.Threshold();
        threshold.setHighCpuPercentage(1.0);
        collector.setThreshold(threshold);

        // Spark
        MetricsConfigurationDocument.Spark spark = new MetricsConfigurationDocument.Spark();
        spark.setSparkAlertUrl("http://localhost:8080/api/metrics/spark");
        spark.setSchedulerIntervalMs(1000);
        spark.setInitialDelayMs(1500);
        collector.setSpark(spark);

        // Aggregated
        MetricsConfigurationDocument.Aggregated aggregated = new MetricsConfigurationDocument.Aggregated();
        aggregated.setAggregatedUrl("http://localhost:8080/api/metrics/aggregated");
        aggregated.setSchedulerIntervalMs(60000);
        aggregated.setInitialDelayMs(3000);
        collector.setAggregated(aggregated);

        // Retry
        MetricsConfigurationDocument.Retry retry = new MetricsConfigurationDocument.Retry();
        retry.setSchedulerIntervalMs(120000);
        retry.setInitialDelayMs(6000);
        collector.setRetry(retry);

        entity.setCollector(collector);
        return entity;
    }
}
