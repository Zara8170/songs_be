package com.example.song_be.domain.song.repository;

import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.querydsl.SongRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SongRepository extends JpaRepository<Song, Long>, SongRepositoryCustom {

}
