package com.example.song_be.domain.song.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationJobMessage {
    private String jobId;
    private Long memberId;
    private List<Long> favoriteSongIds;
    private String strategy; // default | fast | deep
    private Integer topK;
    private String locale; // ko | ja | en
    private String source; // login | manual
    private Instant requestedAt;
    private String idempotencyKey;
    private String traceId;
    private String modelVersion;

    @Builder.Default
    private Integer attempt = 0; // retry attempts
}


