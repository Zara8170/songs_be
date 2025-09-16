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

/**
 * 애니메이션 및 애니송 관리 API 컨트롤러
 * 애니메이션 정보 조회 및 관련 애니송 검색 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;

    /**
     * 모든 애니메이션 목록 조회
     * 
     * @param member 인증된 회원 정보
     * @return 애니메이션 목록
     */
    @GetMapping
    public ResponseEntity<List<AnimeResponseDTO>> getAllAnimes(
            @AuthenticationPrincipal MemberDTO member) {
        List<AnimeResponseDTO> animes = animeService.getAllAnimes();
        return ResponseEntity.ok(animes);
    }

    /**
     * 특정 애니메이션 상세 조회
     * 
     * @param animeId 조회할 애니메이션 ID
     * @param member 인증된 회원 정보
     * @return 애니메이션 상세 정보
     */
    @GetMapping("/{animeId}")
    public ResponseEntity<AnimeResponseDTO> getAnimeById(
            @PathVariable Long animeId,
            @AuthenticationPrincipal MemberDTO member) {
        AnimeResponseDTO anime = animeService.getAnimeById(animeId);
        return ResponseEntity.ok(anime);
    }

    /**
     * 특정 애니메이션의 관련 애니송 목록 조회
     * 
     * @param animeId 애니메이션 ID
     * @param member 인증된 회원 정보
     * @return 애니송 목록
     */
    @GetMapping("/{animeId}/songs")
    public ResponseEntity<List<AniSongResponseDTO>> getSongsByAnimeId(
            @PathVariable Long animeId,
            @AuthenticationPrincipal MemberDTO member) {
        List<AniSongResponseDTO> songs = animeService.getSongsByAnimeId(animeId);
        return ResponseEntity.ok(songs);
    }

    /**
     * 애니메이션 제목으로 애니송 검색
     * 
     * @param animeTitle 검색할 애니메이션 제목
     * @param member 인증된 회원 정보
     * @return 검색된 애니송 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<AniSongResponseDTO>> searchByAnimeTitle(
            @RequestParam String animeTitle,
            @AuthenticationPrincipal MemberDTO member) {
        List<AniSongResponseDTO> songs = animeService.findSongsByAnimeTitle(animeTitle);
        return ResponseEntity.ok(songs);
    }


}
