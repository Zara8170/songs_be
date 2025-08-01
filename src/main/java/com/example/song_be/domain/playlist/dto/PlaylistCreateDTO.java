package com.example.song_be.domain.playlist.dto;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.playlist.entity.Playlist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistCreateDTO {
    
    @NotBlank(message = "플레이리스트 제목은 필수입니다.")
    @Size(max = 100, message = "플레이리스트 제목은 100자를 초과할 수 없습니다.")
    private String title;
    
    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
    private String description;
    
    @Builder.Default
    private Boolean isPublic = true;

    public Playlist toEntity(Member member) {
        return Playlist.builder()
                .title(this.title)
                .description(this.description)
                .isPublic(this.isPublic)
                .member(member)
                .build();
    }
}