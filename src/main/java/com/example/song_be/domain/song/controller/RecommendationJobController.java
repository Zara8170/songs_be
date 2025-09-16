package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.dto.*;
import com.example.song_be.domain.song.service.JsonUtils;
import com.example.song_be.domain.song.service.RecommendationJobStore;
import com.example.song_be.domain.song.service.RecommendationPublisher;
import com.example.song_be.security.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;
import com.example.song_be.config.LogbackJsonConfig;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 곡 추천 작업 관리 API 컨트롤러
 * 비동기 곡 추천 작업의 생성, 상태 조회, 결과 조회 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@Slf4j
public class RecommendationJobController {

    private final RecommendationPublisher publisher;
    private final RecommendationJobStore jobStore;
    // No direct template usage here; publishing goes through RecommendationPublisher

    /**
     * 새로운 곡 추천 작업 생성
     * 
     * @param idemKey 멱등성 키 (동일한 요청 중복 방지)
     * @param body 추천 작업 생성 요청 정보
     * @param member 인증된 회원 정보
     * @return 작업 ID 및 상태 URL
     */
    @PostMapping("/jobs")
    public ResponseEntity<?> createJob(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                       @RequestBody(required = false) RecommendationJobCreateRequest body,
                                       @AuthenticationPrincipal MemberDTO member) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Rate limit: 3 per minute, 50 per day
        if (jobStore.isRateLimited(member.getId(), 3, 50)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        String jobId = UUID.randomUUID().toString();
        // Idempotency handling (60s window)
        if (StringUtils.hasText(idemKey)) {
            String stored = jobStore.setIdempotency(member.getId(), idemKey, jobId);
            if (!stored.equals(jobId)) {
                Map<String, String> conflict = Map.of(
                        "jobId", stored,
                        "status", "queued",
                        "statusUrl", "/api/recommendation/jobs/" + stored
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(conflict);
            }
        }

        MDC.put(LogbackJsonConfig.MDC_JOB_ID, jobId);
        jobStore.setQueued(jobId);

        RecommendationJobMessage msg = RecommendationJobMessage.builder()
                .jobId(jobId)
                .memberId(member.getId())
                .favoriteSongIds(body != null ? body.getFavoriteSongIds() : null)
                .strategy(body != null && StringUtils.hasText(body.getStrategy()) ? body.getStrategy() : "default")
                .topK(body != null && body.getTopK() != null ? body.getTopK() : 50)
                .locale(body != null ? body.getLocale() : null)
                .source(body != null && StringUtils.hasText(body.getSource()) ? body.getSource() : "manual")
                .requestedAt(Instant.now())
                .idempotencyKey(idemKey)
                .traceId(UUID.randomUUID().toString())
                .modelVersion("v1")
                .build();

        publisher.publish(msg);

        Map<String, String> response = Map.of(
                "jobId", jobId,
                "status", "queued",
                "statusUrl", "/api/recommendation/jobs/" + jobId
        );
        MDC.remove(LogbackJsonConfig.MDC_JOB_ID);
        return ResponseEntity
                .accepted()
                .body(response);
    }

    /**
     * 특정 추천 작업의 상태 및 결과 조회
     * 
     * @param jobId 조회할 작업 ID
     * @param member 인증된 회원 정보
     * @return 작업 상태, 진행률, 결과 또는 오류 정보
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId,
                                    @AuthenticationPrincipal MemberDTO member) {
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Map<Object, Object>> opt = jobStore.getStatus(jobId);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Map<Object, Object> map = opt.get();

        RecommendationJobStatusResponse resp = RecommendationJobStatusResponse.builder()
                .jobId(jobId)
                .status((String) map.getOrDefault("status", "queued"))
                .progress(Integer.parseInt((String) map.getOrDefault("progress", "0")))
                .submittedAt(parseInstant((String) map.get("submittedAt")))
                .startedAt(parseInstant((String) map.get("startedAt")))
                .finishedAt(parseInstant((String) map.get("finishedAt")))
                .build();

        if ("succeeded".equals(resp.getStatus())) {
            jobStore.getResultJson(jobId).ifPresent(json -> resp.setResult(JsonUtils.fromJson(json, RecommendationResponseFromPythonDTO.class)));
        } else if ("failed".equals(resp.getStatus())) {
            RecommendationJobError error = RecommendationJobError.builder()
                    .code((String) map.getOrDefault("error.code", "MODEL_ERROR"))
                    .message((String) map.getOrDefault("error.message", "Failed"))
                    .build();
            resp.setError(error);
        }

        return ResponseEntity.ok(resp);
    }

    /**
     * 회원의 최신 추천 결과 조회 (캐싱 지원)
     * 
     * @param member 인증된 회원 정보
     * @param headers HTTP 헤더 (ETag 캐시 검증용)
     * @return 최신 추천 결과 또는 304 Not Modified
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatest(@AuthenticationPrincipal MemberDTO member, @RequestHeader HttpHeaders headers) {
        if (member == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<String> json = jobStore.getLatestForMember(member.getId());
        if (json.isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        String body = json.get();
        String etag = '"' + Integer.toHexString(body.hashCode()) + '"';
        if (headers.getIfNoneMatch() != null && headers.getIfNoneMatch().contains(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }
        RecommendationResponseFromPythonDTO dto = JsonUtils.fromJson(body, RecommendationResponseFromPythonDTO.class);
        return ResponseEntity.ok().eTag(etag).body(dto);
    }

    private Instant parseInstant(String s) {
        if (!StringUtils.hasText(s)) return null;
        return Instant.parse(s);
    }
}


