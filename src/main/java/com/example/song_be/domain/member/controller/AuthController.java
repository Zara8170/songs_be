package com.example.song_be.domain.member.controller;

import com.example.song_be.domain.member.service.GoogleAuthService;
import com.example.song_be.props.JwtProps;
import com.example.song_be.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtProps jwtProps;

    /**
     * Google ID 토큰 → Access/Refresh 발급
     * */
    @PostMapping("/google")
    public ResponseEntity<Map<String,String>> googleLogin(@RequestBody Map<String,String> body,
                                                          HttpServletResponse response) throws Exception {

        String idToken = body.get("idToken");

        // 서비스에서 {accessToken, refreshToken} 반환
        Map<String,String> tokens = googleAuthService.login(idToken);

        // Refresh Token ⇒ Http‑Only 쿠키 (브라우저만 해당, RN은 필요하면 SecureStorage로)
        CookieUtil.setTokenCookie(response,
                "refreshToken",
                tokens.get("refreshToken"),
                jwtProps.getRefreshTokenExpirationPeriod());

        return ResponseEntity.ok(Map.of("accessToken", tokens.get("accessToken")));
    }

}
