package com.example.song_be.domain.member.controller;

import com.example.song_be.domain.member.dto.JoinRequestDTO;
import com.example.song_be.domain.member.dto.LoginRequestDTO;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.song_be.util.TimeUtil;

import static com.example.song_be.util.TimeUtil.checkTime;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final JwtProps jwtProps;

    @PostMapping("/join")
    public ResponseEntity<?> join(@Valid @RequestBody JoinRequestDTO joinRequestDTO) {
        log.info("Join request: {}", joinRequestDTO);
        memberService.join(joinRequestDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인 , 인증처리 api")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody
                                                      LoginRequestDTO loginRequestDTO,
                                                  HttpServletResponse response) {
        log.info("Login request: {}", loginRequestDTO);
        Map<String, Object> loginClaims = memberService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());

        String refreshToken = loginClaims.get("refreshToken").toString();
        String accessToken = loginClaims.get("accessToken").toString();

        CookieUtil.setTokenCookie(response, "refreshToken", refreshToken, jwtProps.getRefreshTokenExpirationPeriod());

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .email(loginClaims.get("email").toString())
                .nickname(loginClaims.get("nickName").toString())
                .roles((List<String>) loginClaims.get("roleNames"))
                .accessToken(accessToken)
                .build();

        log.info("loginResponseDTO: {}", loginResponseDTO);
        // 로그인 성공시, accessToken, email, name, roles 반환
        return ResponseEntity.ok(loginResponseDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("logout");
        // accessToken은 react 내 redux 상태 지워서 없앰
        // 쿠키 삭제
        CookieUtil.removeTokenCookie(response, "refreshToken");

        return ResponseEntity.ok("logout success!");
    }

    @Operation(summary = "토큰 갱신", description = "refreshToken으로 새로운 accessToken을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class)) }),
            @ApiResponse(responseCode = "401", description = "인증 실패 - 유효하지 않은 refreshToken"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @Parameter(description = "refreshToken 쿠키", required = true)
            @CookieValue(value = "refreshToken") String refreshToken,
            HttpServletResponse response) {
        log.info("refresh refreshToken: {}", refreshToken);

        // ✳️ RefreshToken 검증해서 맴버정보 다시 가져옴!
        Map<String, Object> loginClaims = jwtUtil.validateToken(refreshToken);
        log.info("RefreshToken loginClaims: {}", loginClaims);

        String newAccessToken = jwtUtil.generateToken(loginClaims, jwtProps.getAccessTokenExpirationPeriod());
        String newRefreshToken = jwtUtil.generateToken(loginClaims, jwtProps.getRefreshTokenExpirationPeriod());

        // refreshToken 만료시간이 1시간 이하로 남았다면, 새로 발급
        if (checkTime((Integer) loginClaims.get("exp"))) {
            // 새로 발급
            CookieUtil.setTokenCookie(response, "refreshToken", newRefreshToken, jwtProps.getRefreshTokenExpirationPeriod()); // 1day
        } else {
            // 만료시간이 1시간 이상이면, 기존 refreshToken 그대로
            CookieUtil.setNewRefreshTokenCookie(response, "refreshToken", refreshToken);
        }

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .email(loginClaims.get("email").toString())
                .nickname(loginClaims.get("nickName").toString())
                .roles((List<String>) loginClaims.get("roleNames"))
                .accessToken(newAccessToken)
                .build();

        log.info("refresh loginResponseDTO: {}", loginResponseDTO);
        // refresh 성공시, accessToken, email, name, roles 반환
        return ResponseEntity.ok(loginResponseDTO);
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
