package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.dto.RecommendationRequestDTO;
import com.example.song_be.domain.song.dto.RecommendationResponseFromPythonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RestTemplate restTemplate;

    @Value("${python.server.url}")
    private String pythonServerUrl;

    @PostMapping("/request")
    public ResponseEntity<RecommendationResponseFromPythonDTO> requestRecommendation(@RequestBody RecommendationRequestDTO requestDTO) {
        ResponseEntity<RecommendationResponseFromPythonDTO> response = restTemplate.postForEntity(
                pythonServerUrl + "/recommend",
                requestDTO,
                RecommendationResponseFromPythonDTO.class
        );

        return response;
    }
}
