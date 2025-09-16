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

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 회원 인증, 토큰 관리, 회원 탈퇴 등의 핵심 기능을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;
    private final EntityManager entityManager;

    /**
     * 임시 비밀번호 생성
     * 
     * @return 랜덤 생성된 10자리 임시 비밀번호
     */
    @Override
    public String makeTempPassword() {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 10; i++) {
            buffer.append((char) ((int) (Math.random() * 55) + 65));
        }
        return buffer.toString();
    }

    /**
     * 소셜 로그인용 JWT 토큰 생성
     * 
     * @param dto 회원 정보 DTO
     * @return Access/Refresh 토큰이 포함된 클레임 맵
     */
    @Override
    public Map<String, Object> getSocialClaims(MemberDTO dto) {
        Map<String,Object> claims = dto.getClaims();
        claims.put("accessToken",  jwtUtil.generateAccessToken(claims));
        claims.put("refreshToken", jwtUtil.generateRefreshToken(claims));
        return claims;
    }

    /**
     * 회원 탈퇴 처리
     * 관련 데이터(플레이리스트, 좋아요, 권한) 대상 자동 삭제 및 검증
     * 
     * @param email 탈퇴할 회원의 이메일
     * @throws EntityNotFoundException 회원이 존재하지 않는 경우
     * @throws RuntimeException 데이터 삭제 검증 실패 시
     */
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
        
        // 4. 모든 변경사항을 DB에 반영
        entityManager.flush();
        
        // 5. 실제 삭제 완료 검증
        verifyMemberDeletion(memberId, email);
        
        log.info("회원 탈퇴 완료 - Email: {}, ID: {}", email, memberId);
    }
    
    private void verifyMemberDeletion(Long memberId, String email) {
        log.info("회원 삭제 검증 시작 - ID: {}, Email: {}", memberId, email);
        
        // 회원 삭제 확인
        if (memberRepository.existsById(memberId)) {
            throw new RuntimeException("회원 삭제가 완료되지 않았습니다.");
        }
        
        // Playlist 삭제 확인 (네이티브 쿼리 사용)
        Number playlistCountResult = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM playlist WHERE member_id = :memberId")
                .setParameter("memberId", memberId)
                .getSingleResult();
        long playlistCount = playlistCountResult.longValue();
        
        if (playlistCount > 0) {
            throw new RuntimeException("플레이리스트 삭제가 완료되지 않았습니다. 남은 개수: " + playlistCount);
        }
        
        // SongLike 삭제 확인
        Number songLikeCountResult = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM song_like WHERE member_id = :memberId")
                .setParameter("memberId", memberId)
                .getSingleResult();
        long songLikeCount = songLikeCountResult.longValue();
        
        if (songLikeCount > 0) {
            throw new RuntimeException("좋아요 데이터 삭제가 완료되지 않았습니다. 남은 개수: " + songLikeCount);
        }
        
        // MemberRole 삭제 확인
        Number memberRoleCountResult = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM member_role_list WHERE id = :memberId")
                .setParameter("memberId", memberId)
                .getSingleResult();
        long memberRoleCount = memberRoleCountResult.longValue();
        
        if (memberRoleCount > 0) {
            throw new RuntimeException("회원 권한 데이터 삭제가 완료되지 않았습니다. 남은 개수: " + memberRoleCount);
        }
        
        log.info("회원 삭제 검증 완료 - 모든 관련 데이터가 정상적으로 삭제되었습니다. ID: {}", memberId);
    }

    /**
     * 이메일 중복 확인
     * 
     * @param email 확인할 이메일 주소
     * @return 중복 여부 (true: 중복됨, false: 사용가능)
     */
    @Override
    public boolean checkEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 액세스 토큰 갱신
     * 만료된 액세스 토큰을 리프레시 토큰으로 갱신하는 기능
     * 리프레시 토큰 만료 임박 시 자동 재발급
     * 
     * @param accessToken 만료된 액세스 토큰
     * @return 새로운 액세스 토큰이 포함된 맵
     */
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