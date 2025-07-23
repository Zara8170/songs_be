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

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

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

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CustomJWTException("Invalid or missing Authorization header");
        }
        
        String accessToken = authorizationHeader.substring(7);
        Map<String, Object> newTokens = memberService.refreshTokens(accessToken);

        return ResponseEntity.ok(newTokens);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteMember(@AuthenticationPrincipal MemberDTO member) {
        memberService.deleteMember(member.getEmail());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        log.info("checkEmail email: {}", email);
        return ResponseEntity.ok(memberService.checkEmail(email));
    }

}