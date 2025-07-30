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
public class RecommendationResponseFromPythonDTO {
    
    private List<RecommendationGroup> groups;
    private List<CandidateSong> candidates;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationGroup {
        private String label;
        private String tagline;
        private List<GroupSong> songs;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupSong {
        @JsonProperty("title_jp")
        private String titleJp;
        
        @JsonProperty("title_kr")
        private String titleKr;

        @JsonProperty("title_en")
        private String titleEn;
        
        private String artist;
        
        @JsonProperty("artist_kr")
        private String artistKr;
        
        @JsonProperty("tj_number")
        private String tjNumber;
        
        @JsonProperty("ky_number")
        private String kyNumber;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CandidateSong {
        @JsonProperty("song_id")
        private Integer songId;
        
        @JsonProperty("title_jp")
        private String titleJp;
        
        @JsonProperty("title_kr")
        private String titleKr;

        @JsonProperty("title_en")
        private String titleEn;
        
        private String artist;
        
        @JsonProperty("artist_kr")
        private String artistKr;
        
        private String genre;
        private String mood;
        
        @JsonProperty("tj_number")
        private String tjNumber;
        
        @JsonProperty("ky_number")
        private String kyNumber;
        
        @JsonProperty("recommendation_type")
        private String recommendationType;
        
        @JsonProperty("matched_criteria")
        private List<String> matchedCriteria;
    }
}