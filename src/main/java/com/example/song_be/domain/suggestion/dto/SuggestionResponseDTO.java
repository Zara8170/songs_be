package com.example.song_be.domain.suggestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponseDTO {

    private String message;
    private boolean success;

    public static SuggestionResponseDTO success() {
        return SuggestionResponseDTO.builder()
                .message("건의사항이 성공적으로 접수되었습니다.")
                .success(true)
                .build();
    }

    public static SuggestionResponseDTO error(String message) {
        return SuggestionResponseDTO.builder()
                .message(message)
                .success(false)
                .build();
    }
}
