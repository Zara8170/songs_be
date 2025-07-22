package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.security.MemberDTO;

import java.util.Map;

public interface MemberService {

    /**
     * 회원 임시 비밀번호 발급
     *
     * @return 임시 비밀번호
     */
    String makeTempPassword();

    /**
     * 소셜 로그인 시 클레임 정보 반환
     *
     * @param memberDTO 회원정보 DTO
     * @return 클레임 정보
     */
    Map<String, Object> getSocialClaims(MemberDTO memberDTO);

    /**
     * 회원정보 Entity -> DTO 변환
     *
     * @param member 회원정보
     * @return 회원정보 DTO
     */
    default MemberDTO entityToDTO(Member member) {

        return new MemberDTO(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getPhone(),
                member.getMemberRoleList().stream()
                        .map(Enum::name).toList()
        );
    }

    void deleteMember(String email);

    Boolean checkEmail(String email);
}