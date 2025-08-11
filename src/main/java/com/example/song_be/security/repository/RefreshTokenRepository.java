package com.example.song_be.security.repository;

import com.example.song_be.security.entity.RefreshToken;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.member.id = :memberId")
    Optional<RefreshToken> findByMemberId(@Param("memberId") Long memberId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.member WHERE rt.expiry > :now AND rt.expiry <= :until")
    List<RefreshToken> findExpiringBetween(@Param("now") Long now, @Param("until") Long until);
}
