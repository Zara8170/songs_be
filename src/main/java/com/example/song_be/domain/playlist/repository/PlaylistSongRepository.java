package com.example.song_be.domain.playlist.repository;

import com.example.song_be.domain.playlist.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    // 특정 플레이리스트의 모든 곡 조회 (순서대로)
    @Query("SELECT ps FROM PlaylistSong ps WHERE ps.playlist.playlistId = :playlistId ORDER BY ps.order ASC")
    List<PlaylistSong> findByPlaylistIdOrderByOrder(@Param("playlistId") Long playlistId);

    // 특정 플레이리스트에서 특정 곡 찾기
    @Query("SELECT ps FROM PlaylistSong ps WHERE ps.playlist.playlistId = :playlistId AND ps.song.songId = :songId")
    Optional<PlaylistSong> findByPlaylistIdAndSongId(@Param("playlistId") Long playlistId, @Param("songId") Long songId);

    // 특정 플레이리스트의 마지막 순서 번호 조회
    @Query("SELECT COALESCE(MAX(ps.order), 0) FROM PlaylistSong ps WHERE ps.playlist.playlistId = :playlistId")
    Integer findMaxOrderByPlaylistId(@Param("playlistId") Long playlistId);

    // 특정 플레이리스트의 곡 개수
    @Query("SELECT COUNT(ps) FROM PlaylistSong ps WHERE ps.playlist.playlistId = :playlistId")
    Long countByPlaylistId(@Param("playlistId") Long playlistId);

    // 특정 순서 이후의 모든 곡들 조회 (순서 재정렬용)
    @Query("SELECT ps FROM PlaylistSong ps WHERE ps.playlist.playlistId = :playlistId AND ps.order > :order ORDER BY ps.order ASC")
    List<PlaylistSong> findByPlaylistIdAndOrderGreaterThan(@Param("playlistId") Long playlistId, @Param("order") Integer order);

    // 특정 사용자의 모든 플레이리스트에 있는 곡 ID들 조회 (추천 시스템용)
    @Query("SELECT DISTINCT ps.song.songId FROM PlaylistSong ps WHERE ps.playlist.member.id = :memberId")
    List<Long> findSongIdsByMemberId(@Param("memberId") Long memberId);
}