package com.example.song_be.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 회원 인증 정보 데이터 전송 객체
 * Spring Security의 User 클래스를 상속하여 인증 및 권한 정보를 관리합니다.
 */
@Getter
@Setter
@ToString(exclude = {"password", "phone"})
public class MemberDTO extends User {

    /** 회원 고유 ID */
    private Long id;
    /** 이메일 주소 */
    private String email;
    /** 비밀번호 */
    private String password;
    /** 전화번호 */
    private String phone;
    /** 권한 목록 */
    private List<String> roleNames = new ArrayList<>();

    /**
     * MemberDTO 생성자
     * 
     * @param id 회원 ID
     * @param email 이메일
     * @param password 비밀번호
     * @param phone 전화번호
     * @param roleNames 권한명 목록
     */
    public MemberDTO(Long id, String email, String password, String phone, List<String> roleNames) {
        // ROLE_ 접두사를 붙여서 권한을 부여
        super(email, password, roleNames.stream().map(str -> new SimpleGrantedAuthority("ROLE_" + str)).collect(Collectors.toList()));
        this.id = id;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.roleNames = roleNames;
    }

    /**
     * JWT 토큰 클레임용 데이터 맵 생성
     * 
     * @return 회원 정보가 담긴 클레임 맵
     */
    public Map<String, Object> getClaims() {

        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("id", this.id);
        dataMap.put("email", this.email);
        dataMap.put("phone", this.phone);
        dataMap.put("roleNames", this.roleNames);

        return dataMap;
    }
}