package com.example.song_be.domain.song.dto;

import com.example.song_be.dto.PageRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SongPageDTO extends PageRequestDTO {
    private String keyword;

    private String lang;

    private String artist;

    private String artist_kr;

    private String lyrics_original;

    private String lyrics_yomi;
}
