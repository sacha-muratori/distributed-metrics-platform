package com.streaming.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "metrics")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDocument {

    @Id
    private String id;

    private String fingerprint;
    private String clientId;
    private String timestamp;

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