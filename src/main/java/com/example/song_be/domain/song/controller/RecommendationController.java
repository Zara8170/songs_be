package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.like.service.SongLikeService;
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

            List<Long> likedSongIds = songLikeService.getLikedSongIds(member.getId());
            
            List<Long> playlistSongIds = playlistSongRepository.findSongIdsByMemberId(member.getId());
            
            Set<Long> allPreferredSongIds = new HashSet<>();
            allPreferredSongIds.addAll(likedSongIds);
            allPreferredSongIds.addAll(playlistSongIds);
            
            if (requestDTO.getFavoriteSongIds() != null) {
                allPreferredSongIds.addAll(requestDTO.getFavoriteSongIds());
            }
            
            if (allPreferredSongIds.isEmpty()) {
                requestDTO.setFavoriteSongIds(Collections.emptyList());
            } else {
                requestDTO.setFavoriteSongIds(new ArrayList<>(allPreferredSongIds));
            }
            requestDTO.setMemberId(String.valueOf(member.getId()));
            
            log.info("Sending recommendation request for member {} with {} preferred songs", 
                    member.getId(), allPreferredSongIds.size());

            ResponseEntity<String> rawResponse = restTemplate.postForEntity(
                    pythonServerUrl + "/recommend",
                    requestDTO,
                    String.class
            );
            
            String rawBody = rawResponse.getBody();
            if (rawBody != null) {
                int openBraces = rawBody.length() - rawBody.replace("{", "").length();
                int closeBraces = rawBody.length() - rawBody.replace("}", "").length();
                int openBrackets = rawBody.length() - rawBody.replace("[", "").length();
                int closeBrackets = rawBody.length() - rawBody.replace("]", "").length();
                
                if (openBraces != closeBraces || openBrackets != closeBrackets) {
                    throw new RuntimeException("AI 서버 응답이 불완전합니다.");
                }
            }
            
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