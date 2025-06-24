package com.example.song_be.domain.like.controller;

import com.example.song_be.domain.like.dto.SongLikeDTO;
import com.example.song_be.domain.like.service.SongLikeService;
import com.example.song_be.security.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class SongLikeController {

    private final SongLikeService songLikeService;

    @PostMapping("/songs/{songId}/likes")
    public ResponseEntity<SongLikeDTO> like(
            @PathVariable Long songId,
            @AuthenticationPrincipal MemberDTO memberDTO
    ) {
        Long memberId = memberDTO.getId();
        SongLikeDTO dto = songLikeService.addLike(memberId, songId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/songs/{songId}/likes")
    public ResponseEntity<Void> unlike(
            @PathVariable Long songId,
            @AuthenticationPrincipal MemberDTO memberDTO
    ) {
        Long memberId = memberDTO.getId();
        songLikeService.removeLike(memberId, songId);
        return ResponseEntity.noContent().build();
    }


}
