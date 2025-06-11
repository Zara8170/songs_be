package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.dto.JoinRequestDTO;
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

    private final CustomUserDetailService userDetailService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProps jwtProps;
    private final JWTUtil jwtUtil;

    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> login(String email, String password) {
        MemberDTO memberAuthDTO = (MemberDTO) userDetailService.loadUserByUsername(email);
        log.info("email: {}, password: {} ", email, password);

        if(!passwordEncoder.matches(password, memberAuthDTO.getPassword())) {
            throw new RuntimeException("비밀번호가 틀렸습니다.");
        }

        Map<String, Object> memberClaims = memberAuthDTO.getClaims();

        String accessToken = jwtUtil.generateToken(memberClaims, jwtProps.getAccessTokenExpirationPeriod());
        String refreshToken = jwtUtil.generateToken(memberClaims, jwtProps.getRefreshTokenExpirationPeriod());

        memberClaims.put("accessToken", accessToken);
        memberClaims.put("refreshToken", refreshToken);

        return memberClaims;
    }

    @Override
    public void join(JoinRequestDTO joinRequestDTO) {
        memberRepository.findByEmail(joinRequestDTO.getEmail())
                .ifPresent(member -> {
                    throw new IllegalArgumentException("이미 존재하는 회원입니다!");
                });

        joinRequestDTO.setPassword(passwordEncoder.encode(joinRequestDTO.getPassword()));
        Member member = Member.from(joinRequestDTO);

        memberRepository.save(member);
    }

    @Override
    public String makeTempPassword() {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 10; i++) {
            buffer.append((char) ((int) (Math.random() * 55) + 65));
        }
        return buffer.toString();
    }

    @Override
    public Map<String, Object> getSocialClaims(MemberDTO memberDTO) {
        Map<String, Object> claims = memberDTO.getClaims();
        String jwtAccessToken = jwtUtil.generateToken(claims, jwtProps.getAccessTokenExpirationPeriod());
        String jwtRefreshToken = jwtUtil.generateToken(claims, jwtProps.getRefreshTokenExpirationPeriod());

        claims.put("accessToken", jwtAccessToken);
        claims.put("refreshToken", jwtRefreshToken);
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
