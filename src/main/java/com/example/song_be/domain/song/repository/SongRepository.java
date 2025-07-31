package com.example.song_be.domain.song.repository;

import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.querydsl.SongRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long>, SongRepositoryCustom {

    @Query("SELECT s FROM Song s WHERE s.tj_number = :tjNumber")
    Optional<Song> findByTj_number(@Param("tjNumber") Long tj_number);
    
    @Query("SELECT s FROM Song s WHERE s.ky_number = :kyNumber")
    Optional<Song> findByKy_number(@Param("kyNumber") Long ky_number);

}
