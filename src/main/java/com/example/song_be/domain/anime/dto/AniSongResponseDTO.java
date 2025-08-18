package com.example.song_be.domain.anime.dto;

import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.enums.AnimeType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AniSongResponseDTO {
    private Long songId;
    private Long tj_number;
    private Long ky_number;
    private String title_kr;
    private String title_en;
    private String title_yomi;
    private String title_jp;
    private String artist;
    private String artist_kr;
    private Long animeId;
    private String animeTitle;
    private AnimeType animeType;

    public static AniSongResponseDTO fromEntity(Song song) {
        return AniSongResponseDTO.builder()
                .songId(song.getSongId())
                .tj_number(song.getTj_number())
                .ky_number(song.getKy_number())
                .title_kr(song.getTitle_kr())
                .title_en(song.getTitle_en())
                .title_yomi(song.getTitle_yomi())
                .title_jp(song.getTitle_jp())
                .artist(song.getArtist())
                .artist_kr(song.getArtist_kr())
                .animeId(song.getAnime() != null ? song.getAnime().getAnimeId() : null)
                .animeTitle(song.getAnime() != null ? song.getAnime().getTitle() : null)
                .animeType(song.getAnimeType())
                .build();
    }
}
