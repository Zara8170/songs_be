package com.example.song_be.domain.anime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.song_be.domain.anime.entity.Anime;
import com.example.song_be.domain.song.entity.Song;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    // 애니메이션 쿼리
    @Query("SELECT a FROM Anime a ORDER BY a.title")
    List<Anime> findAllOrderByTitle();

    @Query("SELECT a FROM Anime a LEFT JOIN FETCH a.songs WHERE a.animeId = :animeId")
    Optional<Anime> findByIdWithSongs(@Param("animeId") Long animeId);

    @Query("SELECT s FROM Song s WHERE s.anime.animeId = :animeId AND s.animeType IN ('OP', 'ED', 'INSERT', 'OST') ORDER BY CASE WHEN s.animeType = 'OP' THEN 1 WHEN s.animeType = 'ED' THEN 2 WHEN s.animeType = 'INSERT' THEN 3 WHEN s.animeType = 'OST' THEN 4 END, s.songId")
    List<Song> findOpEdSongsByAnimeId(@Param("animeId") Long animeId);

    @Query("SELECT COUNT(s) FROM Song s WHERE s.anime.animeId = :animeId AND s.animeType IN ('OP', 'ED', 'INSERT', 'OST')")
    Long countSongsByAnimeId(@Param("animeId") Long animeId);

    // 애니송 쿼리 (Song 엔티티 대상)
    @Query("SELECT s FROM Song s JOIN s.anime a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :animeTitle, '%'))")
    List<Song> findSongsByAnimeTitleContainingIgnoreCase(@Param("animeTitle") String animeTitle);
    
    @Query("SELECT s FROM Song s WHERE s.anime IS NOT NULL")
    List<Song> findAllAnimeSongs();
}
