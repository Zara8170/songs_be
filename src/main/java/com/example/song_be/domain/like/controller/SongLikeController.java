package com.example.song_be.domain.like.controller;

import com.example.song_be.domain.like.dto.SongLikeCountDTO;
import com.example.song_be.domain.like.dto.SongLikeDTO;
import com.example.song_be.domain.like.service.SongLikeService;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.security.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 곡 좋아요 관리 API 컨트롤러
 * 곡에 대한 좋아요 추가/제거 및 좋아요 통계 조회 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class SongLikeController {

    private final SongLikeService songLikeService;

    /**
     * 곡 좋아요 추가/삭제 토글
     * 
     * @param songId 좋아요를 토글할 곡 ID
     * @param user 인증된 회원 정보
     * @return 좋아요 상태 정보
     */
    @PostMapping("/songs/{songId}")
    public ResponseEntity<SongLikeDTO> toggleLike(
            @PathVariable Long songId,
            @AuthenticationPrincipal MemberDTO user
    ) {
        SongLikeDTO dto = songLikeService.toggleLike(user.getId(), songId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 사용자가 좋아요한 곡 목록 조회
     * 
     * @param user 인증된 회원 정보
     * @return 좋아요한 곡 목록
     */
    @GetMapping
    public ResponseEntity<List<SongDTO>> myLikes(
            @AuthenticationPrincipal MemberDTO user
    ) {
        return ResponseEntity.ok(songLikeService.getLikedSongs(user.getId()));
    }

    /**
     * 특정 곡의 좋아요 개수 조회
     * 
     * @param songId 좋아요 개수를 조회할 곡 ID
     * @return 좋아요 개수
     */
    @GetMapping("/songs/{songId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long songId) {
        long count = songLikeService.getLikeCount(songId);
        return ResponseEntity.ok(count);
    }

    /**
     * 좋아요가 있는 모든 곡들의 좋아요 통계 조회 (좋아요 많은 순)
     * 
     * @return 곡별 좋아요 통계 목록
     */
    @GetMapping("/songs/counts")
    public ResponseEntity<List<SongLikeCountDTO>> getAllSongsWithLikes() {
        List<SongLikeCountDTO> counts = songLikeService.getAllSongsWithLikes();
        return ResponseEntity.ok(counts);
    }
}
