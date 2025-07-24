package com.example.song_be.domain.like.dao;

import com.example.song_be.domain.like.entity.SongLike;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SongLikeDAO {

    private Long likeId;
    private Long songId;
    private Long memberId;

    private boolean liked;
    private LocalDateTime createdAt;

    public static SongLikeDAO fromEntity(SongLike e) {
        return SongLikeDAO.builder()
                .likeId(e.getId())
                .songId(e.getSong().getSongId())
                .memberId(e.getMember().getId())
                .liked(true)
                .createdAt(e.getCreatedAt())
                .build();
    }

    /** 좋아요 취소(행 삭제) 후 반환용 */
    public static SongLikeDAO unliked(Long songId, Long memberId) {
        return SongLikeDAO.builder()
                .songId(songId)
                .memberId(memberId)
                .liked(false)
                .build();
    }
}
