package com.example.song_be.domain.song.document;

import com.example.song_be.domain.song.entity.Song;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "songs")
public class SongDocument {
    @Id
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

    public static SongDocument fromEntity(Song song) {
        return SongDocument.builder()
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

    public Song toEntity() {
        return Song.builder()
                .songId(this.songId)
                .tj_number(this.tj_number)
                .ky_number(this.ky_number)
                .title_kr(this.title_kr)
                .title_en(this.title_en)
                .title_jp(this.title_jp)
                .title_yomi(this.title_yomi)
                .lang(this.lang)
                .artist(this.artist)
                .artist_kr(this.artist_kr)
                .lyrics_original(this.lyrics_original)
                .lyrics_kr(this.lyrics_kr)
                .build();
    }
}
