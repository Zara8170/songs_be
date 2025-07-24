package com.example.song_be.util;

import com.example.song_be.exception.CustomJWTException;
import com.example.song_be.props.JwtProps;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class JWTUtil {

    private final JwtProps jwtProps;

    /** 공통 키 */
    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProps.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    /** Access Token (minutes = accessTokenExpirationPeriod) */
    public String generateAccessToken(Map<String, Object> claims) {
        return build(claims, jwtProps.getAccessTokenExpirationPeriod());
    }

    /** Refresh Token (minutes = refreshTokenExpirationPeriod) */
    public String generateRefreshToken(Map<String, Object> claims) {
        return build(claims, jwtProps.getRefreshTokenExpirationPeriod());
    }

    /** 토큰 생성 공통 로직 */
    private String build(Map<String, Object> claims, long minutes) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(minutes).toInstant()))
                .signWith(secretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 검증 & Claims 반환 */
    public Map<String, Object> validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** 토큰 만료까지 남은 시간이 특정 분 이하인지 체크. */
    public boolean willExpireWithin(String token, long minutes) {
        try {
            long remainingMillis = getExpiryMillis(token) - System.currentTimeMillis();
            return remainingMillis <= minutes * 60 * 1000;
        } catch (Exception e) {
            // 토큰이 유효하지 않거나 만료된 경우
            return true;
        }
    }

    /** 만료 무시 파싱 옵션 */
    public Map<String,Object> parseIgnoreExpiration(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("Token has expired, but parsing claims.");
            return e.getClaims();
        }
    }

    public Long getExpiryMillis(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .getTime();
    }
}