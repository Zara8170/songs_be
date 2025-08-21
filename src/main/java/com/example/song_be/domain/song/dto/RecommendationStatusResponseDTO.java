package com.example.song_be.domain.song.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationStatusResponseDTO {
    
    private String status;
    private String message;
    
    @JsonProperty("generated_date")
    private String generatedDate;
}
