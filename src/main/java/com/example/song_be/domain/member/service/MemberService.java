package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberService {

    Member upsertAndTouch(String tempId);

    List<Member> findInactiveSince(LocalDateTime cutoff);

    int deactivateInactiveMembers();
}
