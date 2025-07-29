package com.example.song_be.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        ObjectMapper objectMapper = new ObjectMapper();
        String msg = objectMapper.writeValueAsString(Map.of(
                "error", "ERROR_ACCESS_TOKEN",
                "message", "유효하지 않은 토큰입니다",
                "path", requestURI
        ));

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter printWriter = response.getWriter();
        printWriter.println(msg);
        printWriter.close();
    }
}
