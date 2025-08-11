package com.example.song_be.domain.song.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationJobCreateRequest {
    @JsonProperty("favorite_song_ids")
    private List<Long> favoriteSongIds;
    private String strategy; // default | fast | deep
    @JsonProperty("top_k")
    private Integer topK;
    private String locale; // ko | ja | en
    private String source; // login | manual
}
