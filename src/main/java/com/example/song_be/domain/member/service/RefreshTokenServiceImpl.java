package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.exception.CustomJWTException;
import com.example.song_be.security.entity.RefreshToken;
import com.example.song_be.security.repository.RefreshTokenRepository;
import com.example.song_be.util.AesUtil;
import com.example.song_be.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final AesUtil aesUtil;
    private final JWTUtil jwtUtil;

    private static final String REDIS_KEY_PREFIX = "refresh_token:";
    
    private String getRedisKey(Long memberId) {
        return REDIS_KEY_PREFIX + memberId;
    }

    /**
     * Redis 연결 상태 테스트
     */
    private void testRedisConnection() {
        try {
            log.info("[RefreshTokenService] Testing Redis connection...");
            String testKey = "redis_connection_test";
            String testValue = "test_value_" + System.currentTimeMillis();
            
            // Redis에 테스트 값 저장
            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            log.info("[RefreshTokenService] Redis TEST WRITE successful: key={}, value={}", testKey, testValue);
            
            // Redis에서 테스트 값 조회
            String retrievedValue = redisTemplate.opsForValue().get(testKey);
            if (testValue.equals(retrievedValue)) {
                log.info("[RefreshTokenService] Redis TEST READ successful: retrieved={}", retrievedValue);
                log.info("[RefreshTokenService] Redis connection is working properly!");
            } else {
                log.error("[RefreshTokenService] Redis TEST FAILED: expected={}, got={}", testValue, retrievedValue);
            }
            
            // 테스트 키 삭제
            redisTemplate.delete(testKey);
            log.info("[RefreshTokenService] Redis test cleanup completed");
            
        } catch (Exception e) {
            log.error("[RefreshTokenService] Redis connection test FAILED: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getRefreshToken(Long memberId) {
        String redisKey = getRedisKey(memberId);
        log.info("[RefreshTokenService] Getting refresh token for member ID: {} (Redis key: {})", memberId, redisKey);
        
        // Redis 연결 테스트 (첫 번째 호출 시)
        testRedisConnection();
        
        try {
            // 1. Redis에서 먼저 조회 (암호화된 상태)
            log.info("[RefreshTokenService] Attempting to retrieve from Redis...");
            String encryptedTokenFromRedis = redisTemplate.opsForValue().get(redisKey);
            
            if (encryptedTokenFromRedis != null) {
                log.info("[RefreshTokenService] REDIS HIT - Found encrypted refresh token in Redis for member ID: {}", memberId);
                log.info("[RefreshTokenService] Encrypted token from Redis (first 20 chars): {}...", encryptedTokenFromRedis.substring(0, Math.min(20, encryptedTokenFromRedis.length())));
                
                String decryptedToken = aesUtil.decrypt(encryptedTokenFromRedis);
                log.info("[RefreshTokenService] Successfully decrypted token from Redis");
                return decryptedToken;
            }
            
            log.info("[RefreshTokenService] REDIS MISS - Token not found in Redis, checking DB for member ID: {}", memberId);
        } catch (Exception e) {
            log.error("[RefreshTokenService] Redis operation failed: {}, falling back to DB", e.getMessage());
        }
        
        // 2. Redis에 없으면 DB에서 조회
        log.info("[RefreshTokenService] Querying database for refresh token...");
        RefreshToken refreshTokenFromDB = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> {
                    log.error("[RefreshTokenService] Refresh Token not found in DB for member ID: {}", memberId);
                    return new CustomJWTException("Refresh Token not found for member " + memberId);
                });
        
        log.info("[RefreshTokenService] Found refresh token in DB for member ID: {}", memberId);
        
        // 3. DB에서 찾은 토큰을 Redis에 캐싱 (암호화된 상태로)
        String encryptedToken = refreshTokenFromDB.getEncryptedToken();
        long ttl = refreshTokenFromDB.getExpiry() - System.currentTimeMillis();
        
        log.info("[RefreshTokenService] Caching token to Redis - TTL: {} ms", ttl);
        
        if (ttl > 0) {
            try {
                redisTemplate.opsForValue().set(redisKey, encryptedToken, ttl, TimeUnit.MILLISECONDS);
                log.info("[RefreshTokenService] Successfully cached encrypted refresh token in Redis for member ID: {}", memberId);
            } catch (Exception e) {
                log.error("[RefreshTokenService] Failed to cache token in Redis: {}", e.getMessage());
            }
        } else {
            log.warn("[RefreshTokenService] Token already expired, not caching to Redis. TTL: {}", ttl);
        }
        
        String decryptedToken = aesUtil.decrypt(encryptedToken);
        log.info("[RefreshTokenService] Successfully decrypted token from DB");
        return decryptedToken;
    }

    @Override
    @Transactional
    public void saveRefreshToken(Long memberId, String refreshToken, long expiry) {
        String redisKey = getRedisKey(memberId);
        log.info("[RefreshTokenService] ================================");
        log.info("[RefreshTokenService] SAVING refresh token for member ID: {} (Redis key: {})", memberId, redisKey);
        log.info("[RefreshTokenService] Token expiry: {} ({}ms from now)", new java.util.Date(expiry), expiry - System.currentTimeMillis());
        
        // 암호화
        log.info("[RefreshTokenService] Encrypting refresh token...");
        String encryptedToken = aesUtil.encrypt(refreshToken);
        log.info("[RefreshTokenService] Encryption completed. Encrypted token (first 20 chars): {}...", encryptedToken.substring(0, Math.min(20, encryptedToken.length())));
        
        // 1. Redis에 저장 (암호화된 상태로)
        long ttl = expiry - System.currentTimeMillis();
        log.info("[RefreshTokenService] Attempting to save to Redis with TTL: {} ms", ttl);
        
        if (ttl > 0) {
            try {
                redisTemplate.opsForValue().set(redisKey, encryptedToken, ttl, TimeUnit.MILLISECONDS);
                log.info("[RefreshTokenService] SUCCESS - Saved encrypted refresh token to Redis for member ID: {}", memberId);
                
                // Redis 저장 확인
                String verifyValue = redisTemplate.opsForValue().get(redisKey);
                if (verifyValue != null && verifyValue.equals(encryptedToken)) {
                    log.info("[RefreshTokenService] VERIFIED - Redis save verification successful");
                } else {
                    log.error("[RefreshTokenService] VERIFICATION FAILED - Redis save verification failed");
                }
            } catch (Exception e) {
                log.error("[RefreshTokenService] FAILED to save to Redis: {}", e.getMessage(), e);
            }
        } else {
            log.warn("[RefreshTokenService] TTL is negative ({}), skipping Redis save", ttl);
        }
        
        // 2. DB에 저장 (암호화된 상태로)
        log.info("[RefreshTokenService] Attempting to save to database...");
        refreshTokenRepository.findByMemberId(memberId)
                .ifPresentOrElse(
                        token -> {
                            log.info("[RefreshTokenService] Updating existing refresh token in DB for member ID: {}", memberId);
                            token.setEncryptedToken(encryptedToken);
                            token.setExpiry(expiry);
                            refreshTokenRepository.save(token);
                            log.info("[RefreshTokenService] Successfully updated existing token in DB");
                        },
                        () -> {
                            log.info("[RefreshTokenService] Creating new refresh token in DB for member ID: {}", memberId);
                            Member member = memberRepository.findById(memberId)
                                    .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));
                            
                            RefreshToken newRefreshToken = RefreshToken.builder()
                                    .member(member)
                                    .encryptedToken(encryptedToken)
                                    .expiry(expiry)
                                    .build();
                            refreshTokenRepository.save(newRefreshToken);
                            log.info("[RefreshTokenService] Successfully created new token in DB");
                        }
                );
        log.info("[RefreshTokenService] Successfully saved encrypted refresh token for member ID: {}", memberId);
        log.info("[RefreshTokenService] ================================");
    }

    @Override
    @Transactional
    public void deleteRefreshToken(Long memberId) {
        String redisKey = getRedisKey(memberId);
        log.info("[RefreshTokenService] Deleting refresh token for member ID: {}", memberId);
        
        // 1. Redis에서 삭제
        redisTemplate.delete(redisKey);
        log.info("[RefreshTokenService] Deleted refresh token from Redis for member ID: {}", memberId);
        
        // 2. DB에서 삭제
        refreshTokenRepository.findByMemberId(memberId)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    log.info("[RefreshTokenService] Deleted refresh token from DB for member ID: {}", memberId);
                });
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        try {
            jwtUtil.validate(refreshToken);
            log.info("[RefreshTokenService] Refresh token validation successful");
            return true;
        } catch (Exception e) {
            log.error("[RefreshTokenService] Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean willExpireSoon(String refreshToken, int minutes) {
        try {
            boolean willExpire = jwtUtil.willExpireWithin(refreshToken, minutes);
            log.info("[RefreshTokenService] Refresh token will expire within {} minutes: {}", minutes, willExpire);
            return willExpire;
        } catch (Exception e) {
            log.error("[RefreshTokenService] Error checking token expiry: {}", e.getMessage());
            return true;
        }
    }
} 