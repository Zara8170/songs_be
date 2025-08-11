package com.example.song_be.domain.song.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationJobCreateResponse {
    private String jobId;
    private String status; // queued
    private String statusUrl;
}


