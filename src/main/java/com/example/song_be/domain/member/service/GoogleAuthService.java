package com.example.song_be.domain.member.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
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
        log.info("[GoogleAuthService] Starting Google login process...");

        // ① ID 토큰 검증
        String clientId = props.getClientId();
        log.info("[GoogleAuthService] Loading Google Client ID: {}", clientId);
        if (clientId == null) {
            log.error("[GoogleAuthService] Google Client ID is NULL. Check environment variables and application.yml configuration.");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(List.of(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenStr);
        if (idToken == null) {
            log.error("[GoogleAuthService] ID Token verification failed. Token is invalid or expired.");
            throw new IllegalArgumentException("Invalid ID token");
        }
        log.info("[GoogleAuthService] ID Token verified successfully.");

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        log.info("[GoogleAuthService] Payload extracted. Email: {}", email);

        // ② 회원 조회·가입
        Member member = memberRepo.findByEmail(email).orElseGet(() -> {
            log.info("  -> Member with email '{}' not found. Creating new member.", email);
            String raw  = memberService.makeTempPassword();
            String enc  = passwordEncoder.encode(raw);
            Member newMember = memberRepo.save(Member.fromSocialMember(email, enc));
            log.info("  -> New member created with ID: {}", newMember.getId());
            return newMember;
        });
        log.info("[GoogleAuthService] Member found or created. Member ID: {}", member.getId());

        // ③ 클레임 → 토큰 2개
        MemberDTO dto   = new MemberDTO(member.getId(), member.getEmail(), member.getPassword(), member.getPhone(),
                List.of(MemberRole.USER.name()));
        Map<String,Object> claims = dto.getClaims();

        String accessToken  = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);
        log.info("[GoogleAuthService] Generated Access Token (first 10 chars): {}", accessToken.substring(0, 10) + "...");
        log.info("[GoogleAuthService] Generated Refresh Token (first 10 chars): {}", refreshToken.substring(0, 10) + "...");

        /* 3) RefreshToken 암호화 후 upsert */
        String enc = aes.encrypt(refreshToken);
        long expMs = jwtUtil.getExpiryMillis(refreshToken);

        refreshRepo.findByMemberId(member.getId())
                .ifPresentOrElse(rt -> {
                    log.info("  -> Updating existing RefreshToken for member ID: {}", member.getId());
                    rt.setEncryptedToken(enc);
                    rt.setExpiry(expMs);
                }, () -> {
                    log.info("  -> Saving new RefreshToken for member ID: {}", member.getId());
                    refreshRepo.save(
                        RefreshToken.builder()
                                .member(member).encryptedToken(enc).expiry(expMs).build());
                });

        LoginResponseDTO responseDTO = LoginResponseDTO.builder()
                .accessToken(accessToken)
                .email(email)
                .roles(List.of(MemberRole.USER.name()))
                .build();
        
        log.info("[GoogleAuthService] Login process finished. Returning DTO: {}", responseDTO);
        return responseDTO;
    }
}
