package com.example.song_be.domain.member.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.song_be.domain.member.service.MemberService;
import com.example.song_be.exception.CustomJWTException;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.util.CookieUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원 관리 API 컨트롤러
 * 회원 인증, 토큰 관리, 회원 정보 관리 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 로그아웃
     * 
     * @param response HTTP 응답 (쿠키 제거용)
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("logout");
        CookieUtil.removeTokenCookie(response, "refreshToken");

        return ResponseEntity.ok("logout success!");
    }

    @Operation(summary = "토큰 갱신", description = "만료된 accessToken과 유효한 refreshToken을 사용하여 새로운 accessToken을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "RefreshToken 만료 또는 유효하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String,Object>> refresh(
            @RequestHeader("Authorization") String authorizationHeader) {

        log.info("=== 🔄 TOKEN REFRESH REQUEST START ===");
        log.info("Authorization header received: {}", authorizationHeader != null ? "YES" : "NO");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.error("❌ Invalid or missing Authorization header: {}", authorizationHeader);
            throw new CustomJWTException("Invalid or missing Authorization header");
        }
        
        String accessToken = authorizationHeader.substring(7);
        log.info("Extracted access token (first 20 chars): {}...", accessToken.substring(0, Math.min(20, accessToken.length())));
        log.info("Calling memberService.refreshTokens()...");
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> newTokens = memberService.refreshTokens(accessToken);
        long endTime = System.currentTimeMillis();
        
        log.info("✅ Token refresh completed successfully in {}ms", endTime - startTime);
        log.info("=== 🔄 TOKEN REFRESH REQUEST END ===");

        return ResponseEntity.ok(newTokens);
    }

    /**
     * 회원 탈퇴
     * 
     * @param member 인증된 회원 정보
     * @return 탈퇴 완료 메시지
     */
    @DeleteMapping
    public ResponseEntity<String> deleteMember(@AuthenticationPrincipal MemberDTO member) {
        memberService.deleteMember(member.getEmail());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    /**
     * 이메일 중복 확인
     * 
     * @param email 확인할 이메일 주소
     * @return 중복 여부 (true: 중복됨, false: 사용가능)
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        log.info("checkEmail email: {}", email);
        return ResponseEntity.ok(memberService.checkEmail(email));
    }

}