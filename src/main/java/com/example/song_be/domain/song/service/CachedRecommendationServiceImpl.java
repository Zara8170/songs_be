package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.dto.CachedRecommendationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedRecommendationServiceImpl implements CachedRecommendationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.props.python-server-url}")
    private String pythonServerUrl;
    
    @Override
    public CachedRecommendationResponseDTO getCachedRecommendation(String memberId) {
        try {
            String url = pythonServerUrl + "/recommend/cached";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, String> requestBody = Map.of("memberId", memberId);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            log.info("AI 서버에 캐시된 추천 요청: {}", url);
            
            ResponseEntity<CachedRecommendationResponseDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                CachedRecommendationResponseDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("AI 서버로부터 캐시된 추천 결과 수신 완료");
                return response.getBody();
            } else {
                throw new RuntimeException("AI 서버로부터 유효하지 않은 응답을 받았습니다.");
            }
            
        } catch (Exception e) {
            log.error("캐시된 추천 결과 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("캐시된 추천 결과 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
