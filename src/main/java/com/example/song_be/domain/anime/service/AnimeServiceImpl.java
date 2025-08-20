package com.example.song_be.domain.anime.service;

import com.example.song_be.domain.anime.dto.AnimeResponseDTO;
import com.example.song_be.domain.anime.dto.AniSongResponseDTO;
import com.example.song_be.domain.anime.entity.Anime;
import com.example.song_be.domain.anime.repository.AnimeRepository;
import com.example.song_be.domain.song.entity.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnimeServiceImpl implements AnimeService {

    private final AnimeRepository animeRepository;

    // 애니메이션 관리
    @Override
    public List<AnimeResponseDTO> getAllAnimes() {
        List<Anime> animes = animeRepository.findAllOrderByTitle();
        return animes.stream()
                .map(anime -> {
                    Long songCount = animeRepository.countSongsByAnimeId(anime.getAnimeId());
                    return AnimeResponseDTO.fromEntityWithSongCount(anime, songCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AnimeResponseDTO getAnimeById(Long animeId) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new RuntimeException("Anime not found with id: " + animeId));
        Long songCount = animeRepository.countSongsByAnimeId(animeId);
        return AnimeResponseDTO.fromEntityWithSongCount(anime, songCount);
    }

    // 애니송 검색
    @Override
    public List<AniSongResponseDTO> findSongsByAnimeTitle(String animeTitle) {
        List<Song> songs = animeRepository.findSongsByAnimeTitleContainingIgnoreCase(animeTitle);
        return songs.stream()
                .map(AniSongResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AniSongResponseDTO> getSongsByAnimeId(Long animeId) {
        // OP, ED만 가져오기
        List<Song> songs = animeRepository.findOpEdSongsByAnimeId(animeId);
        
        return songs.stream()
                .map(AniSongResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


}
