package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.dto.RecommendationJobError;
import com.example.song_be.domain.song.dto.RecommendationResponseFromPythonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecommendationJobStore {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_STATUS = "job:status:"; // + jobId
    private static final String KEY_RESULT = "job:result:"; // + jobId
    private static final String KEY_LATEST_BY_MEMBER = "latest:recommendation:"; // + memberId
    private static final String KEY_IDEMPOTENCY = "idem:"; // + memberId:idempotencyKey
    private static final String KEY_RATE_MIN = "rate:min:"; // + memberId:yyyyMMddHHmm
    private static final String KEY_RATE_DAY = "rate:day:"; // + memberId:yyyyMMdd
    private static final Duration TTL = Duration.ofHours(24);

    public void setQueued(String jobId) {
        Map<String, String> map = new HashMap<>();
        map.put("status", "queued");
        map.put("progress", "0");
        map.put("submittedAt", Instant.now().toString());
        redisTemplate.opsForHash().putAll(KEY_STATUS + jobId, map);
        redisTemplate.expire(KEY_STATUS + jobId, TTL);
    }

    public void setRunning(String jobId) {
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "status", "running");
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "startedAt", Instant.now().toString());
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "progress", "5");
    }

    public void setProgress(String jobId, int progress) {
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "progress", Integer.toString(progress));
    }

    public void setSucceeded(String jobId, RecommendationResponseFromPythonDTO result) {
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "status", "succeeded");
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "finishedAt", Instant.now().toString());
        String json = JsonUtils.toJson(result);
        redisTemplate.opsForValue().set(KEY_RESULT + jobId, json, TTL);
    }

    public void setFailed(String jobId, RecommendationJobError error) {
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "status", "failed");
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "finishedAt", Instant.now().toString());
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "error.code", error.getCode());
        redisTemplate.opsForHash().put(KEY_STATUS + jobId, "error.message", error.getMessage());
    }

    public Optional<Map<Object, Object>> getStatus(String jobId) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(KEY_STATUS + jobId);
        if (map == null || map.isEmpty()) return Optional.empty();
        return Optional.of(map);
    }

    public Optional<String> getResultJson(String jobId) {
        String json = redisTemplate.opsForValue().get(KEY_RESULT + jobId);
        return Optional.ofNullable(json);
    }

    public void setLatestForMember(Long memberId, RecommendationResponseFromPythonDTO result) {
        String json = JsonUtils.toJson(result);
        redisTemplate.opsForValue().set(KEY_LATEST_BY_MEMBER + memberId, json);
    }

    public Optional<String> getLatestForMember(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_LATEST_BY_MEMBER + memberId));
    }

    // Idempotency
    public Optional<String> getExistingJobIdForIdempotency(Long memberId, String idempotencyKey) {
        String key = KEY_IDEMPOTENCY + memberId + ":" + idempotencyKey;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public String setIdempotency(Long memberId, String idempotencyKey, String jobId) {
        String key = KEY_IDEMPOTENCY + memberId + ":" + idempotencyKey;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, jobId, Duration.ofSeconds(60));
        if (Boolean.TRUE.equals(ok)) return jobId;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key)).orElse(jobId);
    }

    // Rate limit: per-minute and per-day
    public boolean isRateLimited(Long memberId, int perMinuteLimit, int perDayLimit) {
        Instant now = Instant.now();
        String minuteKey = KEY_RATE_MIN + memberId + ":" + DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.UTC).format(now);
        String dayKey = KEY_RATE_DAY + memberId + ":" + DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        if (minuteCount != null && minuteCount == 1L) {
            redisTemplate.expire(minuteKey, Duration.ofMinutes(1));
        }
        Long dayCount = redisTemplate.opsForValue().increment(dayKey);
        if (dayCount != null && dayCount == 1L) {
            redisTemplate.expire(dayKey, Duration.ofDays(1));
        }
        return (minuteCount != null && minuteCount > perMinuteLimit) || (dayCount != null && dayCount > perDayLimit);
    }
}


