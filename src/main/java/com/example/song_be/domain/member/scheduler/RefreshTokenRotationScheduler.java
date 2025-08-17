package com.example.song_be.domain.member.scheduler;

import com.example.song_be.domain.member.service.RefreshTokenService;
import com.example.song_be.security.entity.RefreshToken;
import com.example.song_be.security.repository.RefreshTokenRepository;
import com.example.song_be.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenRotationScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;

    // 매 10분마다, 앞으로 7일 내 만료되는 리프레시 토큰을 자동 회전
    @Async
    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    public void rotateExpiringRefreshTokens() {
        long now = System.currentTimeMillis();
        long windowMs = Duration.ofDays(7).toMillis(); // 30분 -> 7일로 변경
        long until = now + windowMs;

        List<RefreshToken> expiring = refreshTokenRepository.findExpiringBetween(now, until);
        if (expiring.isEmpty()) {
            log.debug("[Scheduler] No expiring refresh tokens in next {} days", windowMs / (24 * 60 * 60 * 1000));
            return;
        }

        log.info("[Scheduler] Found {} refresh tokens expiring within {} days", expiring.size(), windowMs / (24 * 60 * 60 * 1000));

        // 회전은 저장 시 쓰기 트랜잭션이 필요하므로 개별 저장 단계에서 트랜잭션을 시작한다
        for (RefreshToken tokenEntity : expiring) {
            try {
                Long memberId = tokenEntity.getMember().getId();

                // MemberDTO.getClaims()와 동일한 키로 최소 클레임 구성
                Map<String, Object> claims = new HashMap<>();
                claims.put("id", memberId);
                claims.put("email", tokenEntity.getMember().getEmail());
                claims.put("phone", tokenEntity.getMember().getPhone());
                // Enum -> String 이름 리스트로 변환
                claims.put(
                        "roleNames",
                        tokenEntity.getMember().getMemberRoleList().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList())
                );

                String newRefreshToken = jwtUtil.generateRefreshToken(claims);
                long newExpiry = jwtUtil.getExpiryMillis(newRefreshToken);

                // 저장 (Redis TTL 갱신 + DB upsert)
                refreshTokenService.saveRefreshToken(memberId, newRefreshToken, newExpiry);

                log.info(
                        "[Scheduler] Rotated refresh token for memberId={} (oldExpiry={}, newExpiry={})",
                        memberId, tokenEntity.getExpiry(), newExpiry
                );
            } catch (Exception e) {
                log.error(
                        "[Scheduler] Failed to rotate refresh token (entityId={}) - {}",
                        tokenEntity.getId(), e.getMessage(), e
                );
            }
        }
    }
}


