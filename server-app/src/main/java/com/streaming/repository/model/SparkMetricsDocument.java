package com.streaming.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "spark_metrics")
@Data
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndex(name = "client_timestamp_idx", def = "{'clientId': 1, 'timestamp': 1}")
public class SparkMetricsDocument {

    @Id
    private String id;

    @Indexed
    private String clientId;

    @Indexed
    private String fingerprint;

    @Indexed
    private Instant timestamp;

    private Integer availableProcessors;
    private Double systemCpuUsagePercent;

    private Long usableDiskBytes;
    private Long totalDiskBytes;
    private Long freeDiskBytes;

    private Long totalPhysicalMemoryBytes;
    private Long freePhysicalMemoryBytes;
    private Long usedPhysicalMemoryBytes;

    private String hostname;
    private String ipaddress;
}