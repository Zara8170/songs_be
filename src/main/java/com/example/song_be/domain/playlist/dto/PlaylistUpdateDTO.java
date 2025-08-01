package com.example.song_be.domain.playlist.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistUpdateDTO {
    
    @Size(max = 100, message = "플레이리스트 제목은 100자를 초과할 수 없습니다.")
    private String title;
    
    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
    private String description;
    
    private Boolean isPublic;
}