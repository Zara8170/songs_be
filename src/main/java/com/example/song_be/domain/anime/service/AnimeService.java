package com.example.song_be.domain.anime.service;

import com.example.song_be.domain.anime.dto.AnimeResponseDTO;
import com.example.song_be.domain.anime.dto.AniSongResponseDTO;


import java.util.List;

public interface AnimeService {
    // 애니메이션 관리
    List<AnimeResponseDTO> getAllAnimes();
    AnimeResponseDTO getAnimeById(Long animeId);
    
    // 애니송 검색
    List<AniSongResponseDTO> findSongsByAnimeTitle(String animeTitle);
    List<AniSongResponseDTO> getSongsByAnimeId(Long animeId);
    

}
