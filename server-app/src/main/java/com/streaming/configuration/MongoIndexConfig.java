package com.streaming.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

import java.time.Duration;

@Configuration
public class MongoIndexConfig {

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @PostConstruct
    public void initIndexes() {
        // TTL index on "timestamp" for spark_metrics collection
        Index ttlIndex = new Index()
                .on("timestamp", Sort.Direction.ASC)
                .expire(Duration.ofSeconds(86400)); // 1 day

        reactiveMongoTemplate.indexOps("spark_metrics").ensureIndex(ttlIndex);
    }
}
