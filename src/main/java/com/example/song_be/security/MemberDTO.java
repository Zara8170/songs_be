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

@Getter
@Setter
@ToString(exclude = {"password", "phone"})
public class MemberDTO extends User {

    private Long id;
    private String email;
    private String password;
    private String phone;
    private List<String> roleNames = new ArrayList<>();

    public MemberDTO(Long id, String email, String password, String phone, List<String> roleNames) {
        // ROLE_ 접두사를 붙여서 권한을 부여
        super(email, password, roleNames.stream().map(str -> new SimpleGrantedAuthority("ROLE_" + str)).collect(Collectors.toList()));
        this.id = id;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.roleNames = roleNames;
    }

    public Map<String, Object> getClaims() {

        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("id", this.id);
        dataMap.put("email", this.email);
        dataMap.put("phone", this.phone);
        dataMap.put("roleNames", this.roleNames);

        return dataMap;
    }
}