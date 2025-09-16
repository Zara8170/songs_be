package com.example.song_be.domain.like.dto;

import com.example.song_be.domain.like.dao.SongLikeDAO;
import com.example.song_be.domain.like.entity.SongLike;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 곡 좋아요 데이터 전송 객체
 * 회원의 곡에 대한 좋아요 상태 정보를 담습니다.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SongLikeDTO {

    /** 곡 ID */
    private Long songId;

    /** 회원 ID */
    private Long memberId;

    /** 좋아요 여부 */
    private boolean liked;

    /** 좋아요 생성 일시 */
    private LocalDateTime createdAt;

    /**
     * SongLikeDAO로부터 SongLikeDTO 생성
     * 
     * @param dao SongLikeDAO 객체
     * @return SongLikeDTO 객체
     */
    public static SongLikeDTO fromDao(SongLikeDAO dao) {
        return SongLikeDTO.builder()
                .songId(dao.getSongId())
                .memberId(dao.getMemberId())
                .liked(dao.isLiked())
                .createdAt(dao.getCreatedAt())
                .build();
    }

    /**
     * SongLike 엔티티로부터 SongLikeDTO 생성
     * 
     * @param like SongLike 엔티티
     * @return SongLikeDTO 객체
     */
    public static SongLikeDTO fromEntity(SongLike like) {
        return SongLikeDTO.builder()
                .songId(like.getSong().getSongId())
                .memberId(like.getMember().getId())
                .liked(true)
                .createdAt(like.getCreatedAt())
                .build();
    }
}
