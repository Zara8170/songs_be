package com.example.song_be.domain.suggestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionEmailMessage {

    private String userEmail;
    private String title;
    private String content;
    private LocalDateTime submittedAt;
    private int retryCount;

    public static SuggestionEmailMessage from(String userEmail, SuggestionRequestDTO request) {
        return SuggestionEmailMessage.builder()
                .userEmail(userEmail)
                .title(request.getTitle())
                .content(request.getContent())
                .submittedAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }

    public SuggestionEmailMessage incrementRetry() {
        this.retryCount++;
        return this;
    }
}
