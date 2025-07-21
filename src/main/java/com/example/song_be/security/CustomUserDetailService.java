//package com.example.song_be.security;
//
//
//import com.example.song_be.domain.member.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class CustomUserDetailService implements UserDetailsService {
//
//    private final MemberRepository memberRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        log.info("loadUserByUsername: {}", username);
//
//        MemberDTO memberDTO = new MemberDTO(
//                member.getId()
//        );
//
//        log.info("loadUserByUsername result memberDTO: {}", memberDTO);
//
//        return memberDTO;
//
//    }
//}
