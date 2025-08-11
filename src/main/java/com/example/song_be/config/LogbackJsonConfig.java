package com.example.song_be.config;

// Note: Actual logback JSON encoder is configured via logback-spring.xml.
// This class defines MDC keys used for structured logging.
public final class LogbackJsonConfig {
    private LogbackJsonConfig() {}

    public static final String MDC_JOB_ID = "jobId";
    public static final String MDC_MEMBER_ID = "memberId";
    public static final String MDC_TRACE_ID = "traceId";
}


