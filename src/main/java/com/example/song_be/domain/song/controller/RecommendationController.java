package com.example.song_be.domain.song.controller;

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
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${python.server.url}")
    private String pythonServerUrl;

    @PostMapping("/request")
    public ResponseEntity<String> requestRecommendation(
            @RequestBody RecommendationRequestDTO requestDTO,
            @AuthenticationPrincipal MemberDTO member) {

        try {
            if (member == null) {
                throw new IllegalStateException("인증이 필요합니다.");
            }

            if (requestDTO.getFavoriteSongIds() == null) {
                requestDTO.setFavoriteSongIds(Collections.emptyList());
            }

            requestDTO.setMemberId(String.valueOf(member.getId()));

            ResponseEntity<String> rawResponse = restTemplate.postForEntity(
                    pythonServerUrl + "/recommend",
                    requestDTO,
                    String.class
            );
            
            String rawBody = rawResponse.getBody();
            if (rawBody != null) {
                // JSON 구조 검증
                int openBraces = rawBody.length() - rawBody.replace("{", "").length();
                int closeBraces = rawBody.length() - rawBody.replace("}", "").length();
                int openBrackets = rawBody.length() - rawBody.replace("[", "").length();
                int closeBrackets = rawBody.length() - rawBody.replace("]", "").length();
                
                if (openBraces != closeBraces || openBrackets != closeBrackets) {
                    throw new RuntimeException("AI 서버 응답이 불완전합니다.");
                }
            }
            
            // String을 객체로 변환하여 유효성 검증
            RecommendationResponseFromPythonDTO parsedResponse = objectMapper.readValue(rawBody, RecommendationResponseFromPythonDTO.class);

            return ResponseEntity
                    .status(rawResponse.getStatusCode())
                    .headers(rawResponse.getHeaders())  
                    .body(rawBody);

        } catch (Exception e) {
            throw new RuntimeException("AI 서버 연결 실패: " + e.getMessage(), e);
        }
    }
}