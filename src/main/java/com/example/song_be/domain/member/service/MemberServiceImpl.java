package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.util.JWTUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final EntityManager entityManager;

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
    @Transactional
    public void deleteMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        
        Long memberId = member.getId();
        log.info("회원 탈퇴 시작 - Email: {}, ID: {}", email, memberId);
        
        // 1. RefreshToken 삭제
        refreshTokenService.deleteRefreshToken(memberId);
        
        // 2. 현재까지의 변경사항을 DB에 반영
        entityManager.flush();
        
        // 3. 회원 삭제 (연관된 Playlist와 SongLike는 cascade로 자동 삭제됨)
        memberRepository.delete(member);
        
        log.info("회원 탈퇴 완료 - Email: {}, ID: {}", email, memberId);
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
            log.warn("[MemberService] Refresh Token expired for member ID: {}", memberId);
            
            // 만료된 토큰 삭제
            refreshTokenService.deleteRefreshToken(memberId);
            
            // 새로운 Refresh Token 자동 생성 (사용자 정보는 Access Token에서 추출)
            String newRefreshToken = jwtUtil.generateRefreshToken(claims);
            long newExpiry = jwtUtil.getExpiryMillis(newRefreshToken);
            refreshTokenService.saveRefreshToken(memberId, newRefreshToken, newExpiry);
            
            log.info("[MemberService] Generated new Refresh Token for expired token");
            
            // 새로 생성된 토큰으로 계속 진행
            refreshToken = newRefreshToken;
        } else {
            log.info("  -> Refresh Token validated successfully (not expired, not tampered).");
        }

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