package com.example.song_be.domain.like.service;

import com.example.song_be.domain.like.dto.SongLikeDTO;

import java.util.List;

public interface SongLikeService {

    SongLikeDTO addLike(Long memberId, Long songId);

    void removeLike(Long memberId, Long songId);

    List<SongLikeDTO> getLikesBySong(Long songId);

    List<SongLikeDTO> getLikesByMember(Long memberId);
}
