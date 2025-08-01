package com.example.song_be.domain.playlist.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistAddSongDTO {
    
    @NotNull(message = "곡 ID는 필수입니다.")
    private Long songId;
    
    private Integer order; // 지정하지 않으면 마지막에 추가
}