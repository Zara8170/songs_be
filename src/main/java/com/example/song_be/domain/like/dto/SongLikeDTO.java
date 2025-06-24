package com.example.song_be.domain.like.dto;

import com.example.song_be.domain.like.entity.SongLike;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SongLikeDTO {

    private Long id;

    private Long songId;

    private Long memberId;

    private LocalDateTime createdAt;

    public static SongLikeDTO from(SongLike entity) {
        return SongLikeDTO.builder()
                .id(entity.getId())
                .songId(entity.getSong().getSongId())
                .memberId(entity.getMember().getId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
