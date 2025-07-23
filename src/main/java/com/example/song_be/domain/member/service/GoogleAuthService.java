package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.dto.LoginResponseDTO;
import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.enums.MemberRole;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.props.GoogleProps;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.security.entity.RefreshToken;
import com.example.song_be.security.repository.RefreshTokenRepository;
import com.example.song_be.util.AesUtil;
import com.example.song_be.util.JWTUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleProps props;
    private final MemberRepository memberRepo;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final AesUtil aes;
    private final RefreshTokenRepository refreshRepo;

    /** 로그인 + JWT 2종 발급 */
    @Transactional
    public LoginResponseDTO login(String idTokenStr) throws GeneralSecurityException, IOException {

        // ① ID 토큰 검증
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(List.of(props.getClientId()))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenStr);
        if (idToken == null) throw new IllegalArgumentException("Invalid ID token");

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        // ② 회원 조회·가입
        Member member = memberRepo.findByEmail(email).orElseGet(() -> {
            String raw  = memberService.makeTempPassword();
            String enc  = passwordEncoder.encode(raw);
            return memberRepo.save(Member.fromSocialMember(email, enc));
        });

        // ③ 클레임 → 토큰 2개
        MemberDTO dto   = new MemberDTO(member.getId(), member.getEmail(), member.getPassword(), member.getPhone(),
                List.of(MemberRole.USER.name()));
        Map<String,Object> claims = dto.getClaims();

        String accessToken  = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        /* 3) RefreshToken 암호화 후 upsert */
        String enc = aes.encrypt(refreshToken);
        long expMs = jwtUtil.getExpiryMillis(refreshToken);

        refreshRepo.findByMemberId(member.getId())
                .ifPresentOrElse(rt -> {
                    rt.setEncryptedToken(enc);
                    rt.setExpiry(expMs);
                }, () -> refreshRepo.save(
                        RefreshToken.builder()
                                .member(member).encryptedToken(enc).expiry(expMs).build()));

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .email(email)
                .roles(List.of(MemberRole.USER.name()))
                .build();
    }
}
