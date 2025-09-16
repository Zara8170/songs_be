package com.example.song_be.security;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 인증을 위한 사용자 정보 로드 서비스
 * 이메일을 기반으로 회원 정보와 권한을 조회하여 인증 객체를 생성합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 이메일(username)로 사용자 인증 정보 로드
     * 
     * @param username 사용자 이메일
     * @return 회원 정보와 권한이 포함된 UserDetails 객체
     * @throws UsernameNotFoundException 회원이 존재하지 않는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("loadUserByUsername: {}", username);

        Member member = memberRepository.getWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("미존재하는 사용자 email: " + username));

        MemberDTO memberDTO = new MemberDTO(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getPhone(),
                member.getMemberRoleList().stream().map(Enum::name).toList()
        );

        log.info("loadUserByUsername result memberDTO: {}", memberDTO);

        return memberDTO;

    }
}