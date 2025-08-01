package com.example.song_be.domain.playlist.service;

import com.example.song_be.domain.playlist.dto.*;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;

import java.util.List;

public interface PlaylistService {

    PlaylistDTO createPlaylist(PlaylistCreateDTO playlistCreateDTO, Long memberId);
    
    PlaylistDTO getPlaylistById(Long playlistId, Long memberId);
    
    PlaylistDTO updatePlaylist(Long playlistId, PlaylistUpdateDTO playlistUpdateDTO, Long memberId);
    
    void deletePlaylist(Long playlistId, Long memberId);

    // 플레이리스트 목록 조회
    List<PlaylistDTO> getUserPlaylists(Long memberId);
    
    PageResponseDTO<PlaylistDTO> getUserPlaylistsWithPaging(Long memberId, PageRequestDTO pageRequestDTO);
    
    PageResponseDTO<PlaylistDTO> getPublicPlaylists(PageRequestDTO pageRequestDTO);
    
    List<PlaylistDTO> searchUserPlaylists(Long memberId, String title);

    // 플레이리스트 곡 관리
    PlaylistSongDTO addSongToPlaylist(Long playlistId, PlaylistAddSongDTO playlistAddSongDTO, Long memberId);
    
    void removeSongFromPlaylist(Long playlistId, Long songId, Long memberId);
    
    List<PlaylistSongDTO> getPlaylistSongs(Long playlistId, Long memberId);
    
    void reorderPlaylistSongs(Long playlistId, Long songId, Integer newOrder, Long memberId);
}