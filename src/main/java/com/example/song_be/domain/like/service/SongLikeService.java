package com.example.song_be.domain.like.service;

import com.example.song_be.domain.like.dto.SongLikeCountDTO;
import com.example.song_be.domain.like.dto.SongLikeDTO;
import com.example.song_be.domain.song.dto.SongDTO;

import java.util.List;

public interface SongLikeService {

    /**
     * 토글 방식: 이미 좋아요면 취소, 없으면 등록
     */
    SongLikeDTO toggleLike(Long memberId, Long songId);

    /**
     * 로그인 사용자가 좋아요한 노래 id 목록
     */
    List<SongDTO> getLikedSongs(Long memberId);

    long getLikeCount(Long songId);

    List<SongLikeCountDTO> getLikeCounts(List<Long> ids);

    /**
     * 좋아요가 있는 모든 노래들의 좋아요 카운트 조회 (좋아요 많은 순으로 정렬)
     */
    List<SongLikeCountDTO> getAllSongsWithLikes();
}
