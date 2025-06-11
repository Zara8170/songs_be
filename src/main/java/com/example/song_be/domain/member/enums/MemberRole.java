package com.example.song_be.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    USER("유저"), ADMIN("관리자");

    private final String roleName;
}
