package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.entity.Song;

import java.util.List;

public interface SongService {


    List<SongDTO> getSongList();

    SongDTO getSongById(Long id);

    SongDTO createSong(SongDTO songDTO);

    SongDTO updateSong(Long id, SongDTO songDTO);

    void deleteSong(Long id);

    default SongDTO toDTO(Song song) {
        return SongDTO.builder()
                .songId(song.getSongId())
                .tj_number(song.getTj_number())
                .ky_number(song.getKy_number())
                .title_kr(song.getTitle_kr())
                .title_en(song.getTitle_en())
                .title_jp(song.getTitle_jp())
                .title_yomi(song.getTitle_yomi())
                .lang(song.getLang())
                .artist(song.getArtist())
                .artist_kr(song.getArtist_kr())
                .lyrics_original(song.getLyrics_original())
                .lyrics_kr(song.getLyrics_kr())
                .build();
    }

    default Song toEntity(SongDTO dto) {
        return Song.builder()
                .songId(dto.getSongId())
                .tj_number(dto.getTj_number())
                .ky_number(dto.getKy_number())
                .title_kr(dto.getTitle_kr())
                .title_en(dto.getTitle_en())
                .title_jp(dto.getTitle_jp())
                .title_yomi(dto.getTitle_yomi())
                .lang(dto.getLang())
                .artist(dto.getArtist())
                .artist_kr(dto.getArtist_kr())
                .lyrics_original(dto.getLyrics_original())
                .lyrics_kr(dto.getLyrics_kr())
                .build();
    }
}
