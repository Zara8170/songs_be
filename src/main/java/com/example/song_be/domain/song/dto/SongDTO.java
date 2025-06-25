package com.example.song_be.domain.song.dto;


import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.dto.PageRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SongDTO extends PageRequestDTO {
    private Long songId;
    private Long tj_number;
    private Long ky_number;

    private String title_kr;
    private String title_en;
    private String title_jp;
    private String title_yomi;
    private String title_yomi_kr;

    private String lang;

    private String artist;
    private String artist_kr;

    private String lyrics_original;
    private String lyrics_yomi;
    private String lyrics_kr;

    private Long likeCount;
    private Boolean likedByMe;
    private String keyword;

    public SongDocument toDocument() {
        return SongDocument.builder()
                .songId(this.songId)
                .tj_number(this.tj_number)
                .ky_number(this.ky_number)
                .title_kr(this.title_kr)
                .title_en(this.title_en)
                .title_jp(this.title_jp)
                .title_yomi(this.title_yomi)
                .title_yomi_kr(this.title_yomi_kr)
                .lang(this.lang)
                .artist(this.artist)
                .artist_kr(this.artist_kr)
                .lyrics_original(this.lyrics_original)
                .lyrics_yomi(this.lyrics_yomi)
                .lyrics_kr(this.lyrics_kr)
                .build();
    }
}
