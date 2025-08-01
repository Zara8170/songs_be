package com.example.song_be.domain.playlist.repository;

import com.example.song_be.domain.playlist.entity.Playlist;
import com.example.song_be.domain.playlist.repository.querydsl.PlaylistRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long>, PlaylistRepositoryCustom {

    // 특정 사용자의 모든 플레이리스트 조회
    @Query("SELECT p FROM Playlist p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC")
    List<Playlist> findByMemberId(@Param("memberId") Long memberId);

    // 플레이리스트 제목으로 검색 (특정 사용자)
    @Query("SELECT p FROM Playlist p WHERE p.member.id = :memberId AND p.title LIKE %:title% ORDER BY p.createdAt DESC")
    List<Playlist> findByMemberIdAndTitleContaining(@Param("memberId") Long memberId, @Param("title") String title);

    // 플레이리스트와 소유자 정보 함께 조회
    @Query("SELECT p FROM Playlist p JOIN FETCH p.member WHERE p.playlistId = :playlistId")
    Optional<Playlist> findByIdWithMember(@Param("playlistId") Long playlistId);

    // 플레이리스트와 곡 정보 모두 함께 조회
    @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.playlistSongs ps LEFT JOIN FETCH ps.song WHERE p.playlistId = :playlistId")
    Optional<Playlist> findByIdWithSongs(@Param("playlistId") Long playlistId);
}