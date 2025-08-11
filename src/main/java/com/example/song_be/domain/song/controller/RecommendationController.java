package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.like.service.SongLikeService;
import com.example.song_be.domain.song.dto.RecommendationJobCreateRequest;
import com.example.song_be.domain.song.dto.RecommendationJobMessage;
import com.example.song_be.domain.song.service.RecommendationJobStore;
import com.example.song_be.domain.song.service.RecommendationPublisher;
import com.example.song_be.domain.playlist.repository.PlaylistSongRepository;
import com.example.song_be.domain.song.dto.RecommendationRequestDTO;
import com.example.song_be.domain.song.dto.RecommendationResponseFromPythonDTO;
import com.example.song_be.security.MemberDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.slf4j.MDC;
import com.example.song_be.config.LogbackJsonConfig;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RestTemplate restTemplate;
    private final SongLikeService songLikeService;
    private final PlaylistSongRepository playlistSongRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RecommendationPublisher recommendationPublisher;
    private final RecommendationJobStore jobStore;

    @Value("${app.props.python-server-url}")
    private String pythonServerUrl;

    @PostMapping("/request")
    public ResponseEntity<?> requestRecommendation(
            @RequestBody RecommendationRequestDTO requestDTO,
            @AuthenticationPrincipal MemberDTO member) {
        try {
            if (member == null) {
                throw new IllegalStateException("인증이 필요합니다.");
            }

            // 1) 비동기 잡 생성 (하위호환용 long-poll)
            String jobId = java.util.UUID.randomUUID().toString();
            MDC.put(LogbackJsonConfig.MDC_JOB_ID, jobId);
            jobStore.setQueued(jobId);
            RecommendationJobCreateRequest create = RecommendationJobCreateRequest.builder()
                    .favoriteSongIds(requestDTO.getFavoriteSongIds())
                    .strategy("default")
                    .topK(50)
                    .source("manual")
                    .build();
            RecommendationJobMessage msg = RecommendationJobMessage.builder()
                    .jobId(jobId)
                    .memberId(member.getId())
                    .favoriteSongIds(create.getFavoriteSongIds())
                    .strategy(create.getStrategy())
                    .topK(create.getTopK())
                    .locale(requestDTO.getMemberId())
                    .source(create.getSource())
                    .requestedAt(java.time.Instant.now())
                    .traceId(java.util.UUID.randomUUID().toString())
                    .modelVersion("v1")
                    .build();
            recommendationPublisher.publish(msg);

            // 2) 최대 25초 간 상태 polling 후 완료되면 결과 반환, 아니면 202+jobId
            long deadline = System.currentTimeMillis() + 25_000;
            while (System.currentTimeMillis() < deadline) {
                var statusMapOpt = jobStore.getStatus(jobId);
                if (statusMapOpt.isPresent()) {
                    String status = (String) statusMapOpt.get().get("status");
                    if ("succeeded".equals(status)) {
                        var jsonOpt = jobStore.getResultJson(jobId);
                        if (jsonOpt.isPresent()) {
                            return ResponseEntity.ok(jsonOpt.get());
                        }
                        break;
                    } else if ("failed".equals(status)) {
                        return ResponseEntity.status(500).body(statusMapOpt.get());
                    }
                }
                Thread.sleep(500);
            }

            MDC.remove(LogbackJsonConfig.MDC_JOB_ID);
            return ResponseEntity.accepted().body(java.util.Map.of(
                    "jobId", jobId,
                    "status", "queued",
                    "statusUrl", "/api/recommendation/jobs/" + jobId
            ));
        } catch (Exception e) {
            throw new RuntimeException("AI 서버 연결 실패: " + e.getMessage(), e);
        }
    }
}