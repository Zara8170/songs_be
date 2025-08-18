package com.example.song_be.domain.anime.controller;

import com.example.song_be.domain.anime.dto.AnimeResponseDTO;
import com.example.song_be.domain.anime.dto.AniSongResponseDTO;

import com.example.song_be.domain.anime.service.AnimeService;
import com.example.song_be.security.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;

    // 애니메이션 관리
    @GetMapping
    public ResponseEntity<List<AnimeResponseDTO>> getAllAnimes(
            @AuthenticationPrincipal MemberDTO member) {
        List<AnimeResponseDTO> animes = animeService.getAllAnimes();
        return ResponseEntity.ok(animes);
    }

    @GetMapping("/{animeId}")
    public ResponseEntity<AnimeResponseDTO> getAnimeById(
            @PathVariable Long animeId,
            @AuthenticationPrincipal MemberDTO member) {
        AnimeResponseDTO anime = animeService.getAnimeById(animeId);
        return ResponseEntity.ok(anime);
    }

    @GetMapping("/{animeId}/songs")
    public ResponseEntity<List<AniSongResponseDTO>> getSongsByAnimeId(
            @PathVariable Long animeId,
            @AuthenticationPrincipal MemberDTO member) {
        List<AniSongResponseDTO> songs = animeService.getSongsByAnimeId(animeId);
        return ResponseEntity.ok(songs);
    }

    // 애니송 검색
    @GetMapping("/search")
    public ResponseEntity<List<AniSongResponseDTO>> searchByAnimeTitle(
            @RequestParam String animeTitle,
            @AuthenticationPrincipal MemberDTO member) {
        List<AniSongResponseDTO> songs = animeService.findSongsByAnimeTitle(animeTitle);
        return ResponseEntity.ok(songs);
    }


}
