package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.props.JwtProps;
import com.example.song_be.security.CustomUserDetailService;
import com.example.song_be.security.MemberDTO;
import com.example.song_be.util.JWTUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final JwtProps jwtProps;
    private final JWTUtil jwtUtil;

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
    public Boolean checkEmail(String email) {
        return memberRepository.existsByEmail(email);
    }
}