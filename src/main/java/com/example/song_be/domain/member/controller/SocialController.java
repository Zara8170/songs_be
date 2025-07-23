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

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class SocialController {
    private final GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> getMemberFromGoogle(@RequestBody Map<String, String> requestBody) throws GeneralSecurityException, IOException {
        String idToken = requestBody.get("idToken");
        log.info("getMemberFromGoogle idToken: {}", idToken);

        LoginResponseDTO loginResponse = googleAuthService.login(idToken);

        return ResponseEntity.ok(loginResponse);
    }
}