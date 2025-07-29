package com.example.song_be.domain.song.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRequestDTO {
    private String memberId;
    @JsonProperty("favorite_song_ids")
    private List<Long> favoriteSongIds;
}
