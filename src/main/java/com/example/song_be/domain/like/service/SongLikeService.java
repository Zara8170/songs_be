//package com.example.song_be.domain.like.service;
//
//import com.example.song_be.domain.like.dto.SongLikeCountDTO;
//import com.example.song_be.domain.like.dto.SongLikeDTO;
//
//import java.util.List;
//
//public interface SongLikeService {
//
//    /**
//     * 토글 방식: 이미 좋아요면 취소, 없으면 등록
//     */
//    SongLikeDTO toggleLike(String tempId, Long songId);
//
//    /**
//     * 로그인 사용자가 좋아요한 노래 id 목록
//     */
//    List<Long> getLikedSongIds(String tempId);
//
//    long getLikeCount(Long songId);
//
//    List<SongLikeCountDTO> getLikeCounts(List<Long> ids);
//}
