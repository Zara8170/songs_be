package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.dto.CachedRecommendationResponseDTO;

public interface CachedRecommendationService {
    CachedRecommendationResponseDTO getCachedRecommendation(String memberId);
}
