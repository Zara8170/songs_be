package com.example.song_be.domain.member.repository;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.enums.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByTempId(String tempId);

    List<Member> findByLastActiveAtBefore(LocalDateTime cutoff);

    List<Member> findByRole(MemberRole role);
}
