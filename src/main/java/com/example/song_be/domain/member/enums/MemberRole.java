package com.example.song_be.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    USER("회원"), GHOST("유령회원");

    private final String roleName;
}