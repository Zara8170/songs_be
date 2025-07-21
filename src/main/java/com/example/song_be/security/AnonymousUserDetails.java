package com.example.song_be.security;

import com.example.song_be.domain.member.enums.MemberRole;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
@ToString
public class AnonymousUserDetails extends User {

    private final String tempId;
    private final MemberRole role;

    public AnonymousUserDetails(String tempId, MemberRole role) {
        super(tempId, "",
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        this.tempId = tempId;
        this.role = role;
    }
}
