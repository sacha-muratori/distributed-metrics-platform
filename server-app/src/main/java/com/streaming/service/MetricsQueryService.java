package com.streaming.service;

import com.streaming.repository.MetricsRepository;
import com.streaming.repository.model.MetricsDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.*;
import java.time.temporal.ChronoUnit;

@Service
public class MetricsQueryService {

    @Autowired
    private MetricsRepository metricsRepository;

    public Flux<MetricsDocument> getMetricsForToday(String clientId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Instant start = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = start.plus(1, ChronoUnit.DAYS).minusNanos(1);
        return queryByRange(start, end, clientId);
    }

    public Flux<MetricsDocument> getMetricsForDay(String day, String clientId) {
        LocalDate date = LocalDate.parse(day);
        Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = start.plus(1, ChronoUnit.DAYS).minusNanos(1);
        return queryByRange(start, end, clientId);
    }

    public Flux<MetricsDocument> getMetricsForThisPastHour(String clientId) {
        Instant end = Instant.now();
        Instant start = end.minus(1, ChronoUnit.HOURS);
        return queryByRange(start, end, clientId);
    }

    public Flux<MetricsDocument> getMetricsInRange(String day, String fromTime, String toTime, String clientId) {
        LocalDate date = LocalDate.parse(day);
        LocalTime from = LocalTime.parse(fromTime); // format: HH:mm
        LocalTime to = LocalTime.parse(toTime);

        Instant start = date.atTime(from).atZone(ZoneOffset.UTC).toInstant();
        Instant end = date.atTime(to).atZone(ZoneOffset.UTC).toInstant();

        return queryByRange(start, end, clientId);
    }

    private Flux<MetricsDocument> queryByRange(Instant start, Instant end, String clientId) {
        return clientId != null
                ? metricsRepository.findByClientIdAndTimestampBetween(clientId, start, end)
                : metricsRepository.findByTimestampBetween(start, end);
    }
}
