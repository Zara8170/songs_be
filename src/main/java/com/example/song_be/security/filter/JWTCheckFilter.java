package com.example.song_be.security.filter;

import com.example.song_be.security.CustomUserDetailService;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final CustomUserDetailService userDetailService;

    // 해당 필터로직(doFilterInternal)을 수행할지 여부를 결정하는 메서드
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("check uri: " + path);

        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        if(path.startsWith("/api/auth/google")) {
            return true;
        }

        // /api/member/로 시작하는 요청은 필터를 타지 않도록 설정
        if (    path.startsWith("/api/member/check-email")
                || path.startsWith("/api/member/refresh")
                || path.startsWith("/api/member/logout")
                || path.startsWith("/api/member/google")
                || path.startsWith("/api/member/social")
        ) {
            return true;
        }

        if(path.startsWith("/api/song/{id}") || path.startsWith("/api/song/batch")) {
            return true;
        }

        if(path.startsWith("/api/es/song") || path.startsWith("/api/es/song/{id}")
                || path.startsWith("/api/es/song/list")) {
            return true;
        }

        if (path.startsWith("/api/migration/songs")) {
            return true;
        }

        if (path.startsWith("/api/recommendation/request")) {
            return true;
        }

        // -----
        // health check
        if (path.startsWith("/com/example/login_project/health")) {
            return true;
        }

        // Swagger UI 경로 제외 설정
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs")) {
            return true;
        }
        // h2-console 경로 제외 설정
        if (path.startsWith("/h2-console")) {
            return true;
        }

        // /favicon.ico 경로 제외 설정
        if (path.startsWith("/favicon.ico")) {
            return true;
        }

        // 이메일 경로 제외
        if (path.startsWith("/api/email/send")) {
            return true;
        }


        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("------------------JWTCheckFilter.................");
        log.info("request.getServletPath(): {}", request.getServletPath());
        log.info("..................................................");

        String autHeaderStr = request.getHeader("Authorization");
        log.info("autHeaderStr Authorization: {}", autHeaderStr);

        // 토큰이 없는 비로그인 사용자의 경우
        if (autHeaderStr == null || !autHeaderStr.startsWith("Bearer ")) {
            log.info("Token is null, chain.doFilter");
            // /api/song/list는 비로그인 사용자도 접근 가능해야 하므로 그냥 통과
            if (request.getRequestURI().startsWith("/api/song/list")) {
                filterChain.doFilter(request, response);
                return;
            }
            // 그 외의 경우는 에러 처리 (기존 로직과 유사하게)
            // 또는 SecurityConfig에서 처리하도록 그냥 통과시키고 컨트롤러에서 @AuthenticationPrincipal로 처리해도 됨
            // 여기서는 명시적으로 에러를 발생시키지 않고, 다음 필터로 넘겨서 Security 단에서 처리하도록 함
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Bearer accessToken 형태로 전달되므로 Bearer 제거
            String accessToken = autHeaderStr.substring(7);// Bearer 제거
            // 쿠키로 가져와
            log.info("JWTCheckFilter accessToken: {}", accessToken);

            Map<String, Object> claims = jwtUtil.validate(accessToken);

            log.info("JWT claims: {}", claims);

            MemberDTO memberDTO = (MemberDTO) userDetailService.loadUserByUsername((String) claims.get("email"));

            log.info("memberDTO: {}", memberDTO);
            log.info("memberDto.getAuthorities(): {}", memberDTO.getAuthorities());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(memberDTO, memberDTO.getPassword(), memberDTO.getAuthorities());

            // SecurityContextHolder에 인증 객체 저장
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 다음 필터로 이동
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT Check Error...........");
            log.error("e.getMessage(): {}", e.getMessage());

            ObjectMapper objectMapper = new ObjectMapper();
            String msg = objectMapper.writeValueAsString(Map.of("error", "ERROR_ACCESS_TOKEN"));

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
        }


    }
}