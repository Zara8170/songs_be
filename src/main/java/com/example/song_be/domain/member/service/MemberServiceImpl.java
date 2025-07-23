package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.props.JwtProps;
import com.example.song_be.security.CustomUserDetailService;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.security.entity.RefreshToken;
import com.example.song_be.security.repository.RefreshTokenRepository;
import com.example.song_be.util.AesUtil;
import com.example.song_be.util.JWTUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import com.example.song_be.exception.CustomJWTException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;
    private final AesUtil aesUtil;

    @Override
    public String makeTempPassword() {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 10; i++) {
            buffer.append((char) ((int) (Math.random() * 55) + 65));
        }
        return buffer.toString();
    }

    @Override
    public Map<String, Object> getSocialClaims(MemberDTO dto) {
        Map<String,Object> claims = dto.getClaims();
        claims.put("accessToken",  jwtUtil.generateAccessToken(claims));
        claims.put("refreshToken", jwtUtil.generateRefreshToken(claims));
        return claims;
    }

    @Override
    public void deleteMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원이 존재하지 않습니다."));
        memberRepository.delete(member);
    }

    @Override
    public boolean checkEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public Map<String, Object> refreshTokens(String accessToken) {
        // 1. Access Token(만료) 파싱 → member_id 획득
        Map<String, Object> claims = jwtUtil.parseIgnoreExpiration(accessToken);
        Long memberId = ((Integer) claims.get("id")).longValue();

        // 2. DB에서 Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomJWTException("Refresh Token not found for member " + memberId));

        // 3. Refresh Token 복호화 및 유효성 검증
        String decryptedToken = aesUtil.decrypt(refreshToken.getEncryptedToken());
        jwtUtil.validate(decryptedToken); // 만료 or 위조 시 예외 발생

        // 4. 새로운 Access Token 발급
        String newAccessToken = jwtUtil.generateAccessToken(claims);

        // (선택) Refresh Token 만료 임박 시, 재발급 (예: 7일 이내)
        String newRefreshToken = decryptedToken;
        if (jwtUtil.willExpireWithin(decryptedToken, 7 * 24 * 60)) {
            newRefreshToken = jwtUtil.generateRefreshToken(claims);
            String encryptedNewToken = aesUtil.encrypt(newRefreshToken);
            long newExpiry = jwtUtil.getExpiryMillis(newRefreshToken);

            refreshToken.setEncryptedToken(encryptedNewToken);
            refreshToken.setExpiry(newExpiry);
            refreshTokenRepository.save(refreshToken);
        }

        return Map.of("accessToken", newAccessToken);
    }
}