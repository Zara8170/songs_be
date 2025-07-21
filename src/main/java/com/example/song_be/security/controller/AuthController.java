package com.example.song_be.security.controller;


import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.service.MemberService;
import com.example.song_be.security.entity.RefreshToken;
import com.example.song_be.security.repository.RefreshTokenRepository;
import com.example.song_be.util.JWTUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;
    private final MemberService memberService;
    private final RefreshTokenRepository tokenRepo;

    @PostMapping("/anon")
    public ResponseEntity<?> issueAnon(@RequestBody TempIdRequest dto) {

        Member member = memberService.upsertAndTouch(dto.tempId());

        String access = jwtUtil.generate(
                Map.of("role", member.getRole().name()),
                member.getTempId(), 60);          // 60분

        String refresh = jwtUtil.generate(
                Map.of(), member.getTempId(), 60 * 24 * 7);  // 7일

        tokenRepo.save(RefreshToken.builder()
                .tempId(member.getTempId())
                .accessToken(access)
                .refreshToken(refresh)
                .refreshExpiresAt(LocalDateTime.now().plusDays(7))
                .build());

        return ResponseEntity.ok(Map.of(
                "accessToken", access,
                "refreshToken", refresh));
    }

    /** JSON 파라미터용 record */
    public record TempIdRequest(@NotBlank String tempId) {}
}
