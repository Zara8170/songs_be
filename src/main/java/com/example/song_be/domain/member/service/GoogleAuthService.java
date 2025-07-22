package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.enums.MemberRole;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.props.GoogleProps;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.util.JWTUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleProps props;
    private final MemberRepository memberRepo;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    /** 로그인 + JWT 2종 발급 */
    @Transactional
    public Map<String, String> login(String idTokenStr) throws GeneralSecurityException, IOException {

        // ① ID 토큰 검증
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(List.of(props.getClientId()))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenStr);
        if (idToken == null) throw new IllegalArgumentException("Invalid ID token");

        // ② 회원 조회·가입
        String email = idToken.getPayload().getEmail();
        Member member = memberRepo.findByEmail(email).orElseGet(() -> {
            String raw  = memberService.makeTempPassword();
            String enc  = passwordEncoder.encode(raw);
            return memberRepo.save(Member.fromSocialMember(email, enc));
        });

        // ③ 클레임 → 토큰 2개
        MemberDTO dto   = new MemberDTO(member.getId(), member.getEmail(),
                member.getPassword(), null,
                List.of(MemberRole.USER.name()));
        Map<String,Object> claims = dto.getClaims();

        String accessToken  = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }
}
