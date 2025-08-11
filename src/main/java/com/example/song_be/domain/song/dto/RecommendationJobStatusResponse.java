package com.example.song_be.domain.song.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationJobStatusResponse {
    private String jobId;
    private String status; // queued|running|succeeded|failed|canceled
    private int progress;
    private Instant submittedAt;
    private Instant startedAt;
    private Instant finishedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RecommendationResponseFromPythonDTO result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RecommendationJobError error;
}


