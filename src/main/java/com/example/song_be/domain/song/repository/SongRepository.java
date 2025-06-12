package com.example.song_be.domain.song.repository;

import com.example.song_be.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SongRepository extends JpaRepository<Song, Long> {

}
