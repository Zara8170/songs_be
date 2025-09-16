package com.example.song_be.domain.member.controller;

import com.example.song_be.domain.member.dto.LoginResponseDTO;
import com.example.song_be.domain.member.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * 소셜 로그인 API 컨트롤러
 * Google OAuth를 통한 소셜 로그인 기능을 제공합니다.
 */
@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class SocialController {
    private final GoogleAuthService googleAuthService;

    /**
     * Google OAuth 로그인
     * 
     * @param requestBody Google ID 토큰이 포함된 요청 본문
     * @return 로그인 응답 정보 (JWT 토큰, 회원 정보 등)
     * @throws GeneralSecurityException Google 토큰 검증 실패
     * @throws IOException 네트워크 오류
     */
    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> getMemberFromGoogle(@RequestBody Map<String, String> requestBody) throws GeneralSecurityException, IOException {
        String idToken = requestBody.get("idToken");
        log.info("getMemberFromGoogle idToken: {}", idToken);

        LoginResponseDTO loginResponse = googleAuthService.login(idToken);

        return ResponseEntity.ok(loginResponse);
    }
}