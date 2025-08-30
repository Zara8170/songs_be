package com.example.song_be.domain.suggestion.service;

import com.example.song_be.domain.suggestion.dto.SuggestionRequestDTO;
import com.example.song_be.domain.suggestion.dto.SuggestionResponseDTO;

public interface SuggestionService {
    SuggestionResponseDTO submitSuggestion(String userEmail, SuggestionRequestDTO request);
}
