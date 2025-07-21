package com.example.song_be.security.filter;

import com.example.song_be.domain.member.enums.MemberRole;
import com.example.song_be.security.AnonymousUserDetails;
import com.example.song_be.security.entity.RefreshToken;
import com.example.song_be.security.repository.RefreshTokenRepository;
import com.example.song_be.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository tokenRepo;

    private static final String AUTH_HEADER = "Authorization";
    private static final String REFRESH_HEADER = "X-Refresh-Token";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        String accessToken = parseToken(req.getHeader(AUTH_HEADER));
        if (!StringUtils.hasText(accessToken)) {
            chain.doFilter(req, res);
            return; // 토큰 없는 공용 API 는 SecurityConfig 에서 permitAll
        }

        RefreshToken stored = tokenRepo.findByAccessToken(accessToken).orElse(null);

        try {
            Claims claims = jwtUtil.validate(accessToken);      // ① 서명·만료 검증
            if (stored == null) throw new RuntimeException("DB에 없는 토큰");

            setAuth(claims);                                    // 통과
            chain.doFilter(req, res);
            return;

        } catch (Exception e) {
            log.debug("AccessToken 검증 실패: {}", e.getMessage());
        }

        /* ② AccessToken 실패 → RefreshToken 시도 */
        String refreshToken = parseToken(req.getHeader(REFRESH_HEADER));
        if (!StringUtils.hasText(refreshToken) || stored == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 만료");
            return;
        }

        // refreshToken 만료 확인
        if (stored.getRefreshExpiresAt().isBefore(LocalDateTime.now())
                || !stored.getRefreshToken().equals(refreshToken)) {

            tokenRepo.deleteByTempId(stored.getTempId());               // 만료·위조 → 제거
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "리프레시 만료");
            return;
        }

        /* ③ RefreshToken 유효 → 새 토큰 재발급 */
        String newAccess = jwtUtil.generate(
                Map.of("role", MemberRole.ACTIVE.name()),
                stored.getTempId(), 60);          // 60분

        String newRefresh = jwtUtil.generate(
                Map.of(), stored.getTempId(), 60 * 24 * 7);  // 7일

        stored.setAccessToken(newAccess);
        stored.setRefreshToken(newRefresh);
        stored.setRefreshExpiresAt(LocalDateTime.now().plusDays(7));
        tokenRepo.save(stored);

        res.setHeader(AUTH_HEADER, "Bearer " + newAccess);
        res.setHeader(REFRESH_HEADER, newRefresh);

        // SecurityContext 에도 세팅
        Claims claims = jwtUtil.validate(newAccess);
        setAuth(claims);
        chain.doFilter(req, res);
    }

    private void setAuth(Claims claims) {
        String tempId = claims.getSubject();
        MemberRole role = MemberRole.valueOf((String) claims.get("role"));
        AnonymousUserDetails principal = new AnonymousUserDetails(tempId, role);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String parseToken(String header) {
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }
}
