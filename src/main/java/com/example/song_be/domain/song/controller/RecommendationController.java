package com.example.song_be.domain.song.controller;

import com.example.song_be.config.LogbackJsonConfig;
import com.example.song_be.domain.song.dto.CachedRecommendationResponseDTO;
import com.example.song_be.domain.song.dto.RecommendationJobCreateRequest;
import com.example.song_be.domain.song.dto.RecommendationJobMessage;
import com.example.song_be.domain.song.dto.RecommendationRequestDTO;
import com.example.song_be.domain.song.dto.RecommendationStatusResponseDTO;
import com.example.song_be.domain.song.service.CachedRecommendationService;
import com.example.song_be.domain.song.service.RecommendationJobStore;
import com.example.song_be.domain.song.service.RecommendationPublisher;
import com.example.song_be.security.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationPublisher recommendationPublisher;
    private final RecommendationJobStore jobStore;
    private final CachedRecommendationService cachedRecommendationService;

    @Value("${app.props.python-server-url}")
    private String pythonServerUrl;

    @PostMapping("/request")
    public ResponseEntity<RecommendationStatusResponseDTO> requestRecommendation(
            @RequestBody RecommendationRequestDTO requestDTO,
            @AuthenticationPrincipal MemberDTO member) {
        try {
            if (member == null) {
                throw new IllegalStateException("인증이 필요합니다.");
            }

            // 비동기 추천 작업 시작
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

            MDC.remove(LogbackJsonConfig.MDC_JOB_ID);
            
            // 간단한 상태 응답 반환
            RecommendationStatusResponseDTO response = RecommendationStatusResponseDTO.builder()
                    .status("completed")
                    .message("추천 분석 및 생성이 완료되었습니다.")
                    .generatedDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .build();
                    
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("추천 요청 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("추천 요청 처리에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @PostMapping("/cached")
    public ResponseEntity<CachedRecommendationResponseDTO> getCachedRecommendation(
            @AuthenticationPrincipal MemberDTO member) {
        try {
            if (member == null) {
                throw new IllegalStateException("인증이 필요합니다.");
            }

            log.info("사용자 {}의 캐시된 추천 결과 조회 요청", member.getId());
            
            CachedRecommendationResponseDTO cachedResult = cachedRecommendationService
                    .getCachedRecommendation(String.valueOf(member.getId()));
                    
            return ResponseEntity.ok(cachedResult);
            
        } catch (Exception e) {
            log.error("캐시된 추천 결과 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("캐시된 추천 결과 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }
}