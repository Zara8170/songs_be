package com.example.song_be.domain.member.controller;

import com.example.song_be.domain.member.dto.LoginResponseDTO;
import com.example.song_be.domain.member.service.MemberService;
import com.example.song_be.props.JwtProps;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.util.CookieUtil;
import com.example.song_be.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.example.song_be.util.TimeUtil.checkTime;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final JwtProps jwtProps;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("logout");
        // accessToken은 react 내 redux 상태 지워서 없앰
        // 쿠키 삭제
        CookieUtil.removeTokenCookie(response, "refreshToken");

        return ResponseEntity.ok("logout success!");
    }

    @Operation(summary = "토큰 갱신", description = "refreshToken 쿠키로 새로운 accessToken을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "refreshToken 만료·위조"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/refresh")
    public ResponseEntity<Map<String,String>> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {

        // 1) refreshToken 검증 → Claims 추출
        Map<String,Object> claims = jwtUtil.validate(refreshToken);

        // 2) 새 Access Token 발급
        String newAccess = jwtUtil.generateAccessToken(claims);

        // 3) Refresh Token 남은 만료가 60분 미만이면 교체
        boolean needNewRefresh = jwtUtil.willExpireWithin(refreshToken, 60); // ↓ 필요 util
        String newRefresh = refreshToken;
        if (needNewRefresh) {
            newRefresh = jwtUtil.generateRefreshToken(claims);
            CookieUtil.setTokenCookie(
                    response, "refreshToken", newRefresh, jwtProps.getRefreshTokenExpirationPeriod());
        }

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess
        ));
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