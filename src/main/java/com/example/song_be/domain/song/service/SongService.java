package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;

import java.util.List;

public interface SongService {


    PageResponseDTO<SongDTO> getSongList(PageRequestDTO requestDTO);

    SongDTO getSongById(Long id);

    List<SongDTO> findSongsByIds(List<Long> songIds);

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
                .title_en_kr(song.getTitle_en_kr())
                .title_jp(song.getTitle_jp())
                .title_yomi(song.getTitle_yomi())
                .title_yomi_kr(song.getTitle_yomi_kr())
                .lang(song.getLang())
                .genre(song.getGenre())
                .mood(song.getMood())
                .artist(song.getArtist())
                .artist_kr(song.getArtist_kr())
                .lyrics_original(song.getLyrics_original())
                .lyrics_yomi(song.getLyrics_yomi())
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
                .title_en_kr(dto.getTitle_en_kr())
                .title_jp(dto.getTitle_jp())
                .title_yomi(dto.getTitle_yomi())
                .title_yomi_kr(dto.getTitle_yomi_kr())
                .lang(dto.getLang())
                .genre(dto.getGenre())
                .mood(dto.getMood())
                .artist(dto.getArtist())
                .artist_kr(dto.getArtist_kr())
                .lyrics_original(dto.getLyrics_original())
                .lyrics_yomi(dto.getLyrics_yomi())
                .lyrics_kr(dto.getLyrics_kr())
                .build();
    }
}
