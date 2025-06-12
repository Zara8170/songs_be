package com.example.song_be.domain.song.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SongDTO {
    private Long songId;
    private Long tj_number;
    private Long ky_number;

    private String title_kr;
    private String title_en;
    private String title_jp;
    private String title_yomi;

    private String lang;

    private String artist;
    private String artist_kr;

    private String lyrics_original;
    private String lyrics_kr;
}
