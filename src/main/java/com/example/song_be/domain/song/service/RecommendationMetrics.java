package com.example.song_be.domain.song.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RecommendationMetrics {
    private final MeterRegistry meterRegistry;

    public void incrementEnqueued(String source) {
        Counter.builder("rec_jobs_enqueued").tag("source", nullToNA(source)).register(meterRegistry).increment();
    }

    public void incrementSucceeded(String source) {
        Counter.builder("rec_jobs_succeeded").tag("source", nullToNA(source)).register(meterRegistry).increment();
    }

    public void incrementFailed(String reason) {
        Counter.builder("rec_jobs_failed").tag("reason", nullToNA(reason)).register(meterRegistry).increment();
    }

    public void incrementRetried(int attempt) {
        Counter.builder("rec_jobs_retried").tag("attempt", Integer.toString(attempt)).register(meterRegistry).increment();
    }

    public void observeLatency(Duration d) {
        Timer.builder("rec_job_latency_ms").register(meterRegistry).record(d);
    }

    public void incrementDeadLetter() {
        Counter.builder("rec_jobs_dead_letter").register(meterRegistry).increment();
    }

    private String nullToNA(String v) { return v == null || v.isEmpty() ? "NA" : v; }
}


