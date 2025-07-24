package com.example.song_be.domain.like.controller;

import com.example.song_be.domain.like.dto.SongLikeDTO;
import com.example.song_be.domain.like.service.SongLikeService;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.security.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class SongLikeController {

    private final SongLikeService songLikeService;

    /** 추가·삭제를 한 곳에서 처리 */
    @PostMapping("/songs/{songId}")
    public ResponseEntity<SongLikeDTO> toggleLike(
            @PathVariable Long songId,
            @AuthenticationPrincipal MemberDTO user
    ) {
        SongLikeDTO dto = songLikeService.toggleLike(user.getId(), songId);
        return ResponseEntity.ok(dto);
    }

    /** 내 즐겨찾기 목록 조회 */
    @GetMapping
    public ResponseEntity<List<SongDTO>> myLikes(
            @AuthenticationPrincipal MemberDTO user
    ) {
        return ResponseEntity.ok(songLikeService.getLikedSongs(user.getId()));
    }
}
