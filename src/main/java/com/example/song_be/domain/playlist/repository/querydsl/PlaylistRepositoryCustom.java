package com.example.song_be.domain.playlist.repository.querydsl;

import com.example.song_be.domain.playlist.entity.Playlist;
import com.example.song_be.dto.PageRequestDTO;
import org.springframework.data.domain.Page;

public interface PlaylistRepositoryCustom {

    Page<Playlist> findByMemberIdWithPaging(Long memberId, PageRequestDTO requestDTO);
    
    Page<Playlist> findPublicPlaylistsWithPaging(PageRequestDTO requestDTO);
}