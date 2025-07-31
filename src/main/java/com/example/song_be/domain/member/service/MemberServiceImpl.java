package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.util.JWTUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import com.example.song_be.exception.CustomJWTException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;

    @Override
    public String makeTempPassword() {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 10; i++) {
            buffer.append((char) ((int) (Math.random() * 55) + 65));
        }
        return buffer.toString();
    }

    @Override
    public Map<String, Object> getSocialClaims(MemberDTO dto) {
        Map<String,Object> claims = dto.getClaims();
        claims.put("accessToken",  jwtUtil.generateAccessToken(claims));
        claims.put("refreshToken", jwtUtil.generateRefreshToken(claims));
        return claims;
    }

    @Override
    public void deleteMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        memberRepository.delete(member);
    }

    @Override
    public boolean checkEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public Map<String, Object> refreshTokens(String accessToken) {
        log.info("[MemberService] Starting token refresh process...");
        
        // 1. Access Token(만료) 파싱 → member_id 획득
        Map<String, Object> claims = jwtUtil.parseIgnoreExpiration(accessToken);
        Long memberId = ((Integer) claims.get("id")).longValue();
        log.info("  -> Parsed Member ID from expired Access Token: {}", memberId);

        // 2. Redis/DB에서 Refresh Token 조회
        String refreshToken = refreshTokenService.getRefreshToken(memberId);
        log.info("  -> Found Refresh Token for member ID: {}", memberId);

        // 3. Refresh Token 유효성 검증
        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            log.error("[MemberService] Refresh Token validation failed for member ID: {}", memberId);
            throw new CustomJWTException("Invalid Refresh Token for member " + memberId);
        }
        log.info("  -> Refresh Token validated successfully (not expired, not tampered).");

        // 4. 새로운 Access Token 발급
        String newAccessToken = jwtUtil.generateAccessToken(claims);
        log.info("[MemberService] Generated new Access Token (first 10 chars): {}", newAccessToken.substring(0, 10) + "...");

        // 5. Refresh Token 만료 임박 시, 재발급 (예: 7일 이내)
        if (refreshTokenService.willExpireSoon(refreshToken, 7 * 24 * 60)) {
            log.info("  -> Refresh Token will expire soon. Renewing...");
            String newRefreshToken = jwtUtil.generateRefreshToken(claims);
            long newExpiry = jwtUtil.getExpiryMillis(newRefreshToken);
            
            refreshTokenService.saveRefreshToken(memberId, newRefreshToken, newExpiry);
            log.info("  -> New Refresh Token saved to Redis and DB.");
        }

        Map<String, Object> responseMap = Map.of("accessToken", newAccessToken);
        log.info("[MemberService] Token refresh process finished. Returning new Access Token.");
        return responseMap;
    }
}