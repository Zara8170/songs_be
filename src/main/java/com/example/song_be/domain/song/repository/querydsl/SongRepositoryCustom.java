package com.example.song_be.domain.song.repository.querydsl;

import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.dto.PageRequestDTO;
import org.springframework.data.domain.Page;

public interface SongRepositoryCustom {

    Page<Song> findListBy(PageRequestDTO requestDTO);
}
