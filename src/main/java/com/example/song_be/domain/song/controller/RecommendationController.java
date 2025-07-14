package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.dto.RecommendationRequestDTO;
import com.example.song_be.domain.song.dto.RecommendationResponseFromPythonDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RestTemplate restTemplate;

    @Value("${python.server.url}")
    private String pythonServerUrl;

    @PostMapping("/request")
    public ResponseEntity<RecommendationResponseFromPythonDTO> requestRecommendation(
            @RequestBody RecommendationRequestDTO requestDTO) {

        if (requestDTO.getFavoriteSongIds() == null) {
            requestDTO.setFavoriteSongIds(Collections.emptyList());
        }

        log.info("Sending recommendation request: text={}, favoriteSongIds={}",
                requestDTO.getText(), requestDTO.getFavoriteSongIds());

        return restTemplate.postForEntity(
                pythonServerUrl + "/recommend",
                requestDTO,
                RecommendationResponseFromPythonDTO.class
        );
    }

}