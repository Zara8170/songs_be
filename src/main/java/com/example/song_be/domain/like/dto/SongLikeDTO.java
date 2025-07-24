package com.example.song_be.domain.like.dto;

import com.example.song_be.domain.like.dao.SongLikeDAO;
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

    private Long songId;

    private Long memberId;

    private boolean liked;

    private LocalDateTime createdAt;

    public static SongLikeDTO fromDao(SongLikeDAO dao) {
        return SongLikeDTO.builder()
                .songId(dao.getSongId())
                .memberId(dao.getMemberId())
                .liked(dao.isLiked())
                .createdAt(dao.getCreatedAt())
                .build();
    }

    public static SongLikeDTO fromEntity(SongLike like) {
        return SongLikeDTO.builder()
                .songId(like.getSong().getSongId())
                .memberId(like.getMember().getId())
                .liked(true)
                .createdAt(like.getCreatedAt())
                .build();
    }
}
