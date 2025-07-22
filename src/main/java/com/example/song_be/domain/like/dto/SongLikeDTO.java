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

    private String memberTempId;

    private boolean liked;

    private LocalDateTime createdAt;

    public static SongLikeDTO fromDao(SongLikeDAO dao, String tempId) {
        return SongLikeDTO.builder()
                .songId(dao.getSongId())
                .memberTempId(tempId)
                .liked(dao.isLiked())
                .createdAt(dao.getCreatedAt())
                .build();
    }
}
