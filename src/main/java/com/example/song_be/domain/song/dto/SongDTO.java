package com.example.song_be.domain.song.dto;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.enums.AnimeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 곡 정보 데이터 전송 객체
 * 곡의 기본 정보, 제목(다국어), 가사, 좋아요 정보, 애니메이션 정보를 포함합니다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"lyrics_original", "lyrics_yomi", "lyrics_kr"})
public class SongDTO {
    /** 곡 고유 ID */
    private Long songId;
    /** TJ 노래방 번호 */
    private Long tj_number;
    /** KY 노래방 번호 */
    private Long ky_number;
    /** 한국어 제목 */
    private String title_kr;
    /** 영어 제목 */
    private String title_en;
    /** 영어 제목 한국어 표기 */
    private String title_en_kr;
    /** 일본어 제목 */
    private String title_jp;
    /** 일본어 제목 요미가나 */
    private String title_yomi;
    /** 일본어 제목 요미가나 한국어 표기 */
    private String title_yomi_kr;
    /** 언어 코드 */
    private String lang;
    /** 장르 */
    private String genre;
    /** 분위기/무드 */
    private String mood;
    /** 아티스트명 */
    private String artist;
    /** 아티스트명 한국어 표기 */
    private String artist_kr;
    /** 원본 가사 */
    private String lyrics_original;
    /** 일본어 가사 요미가나 */
    private String lyrics_yomi;
    /** 한국어 번역 가사 */
    private String lyrics_kr;
    /** 좋아요 개수 */
    private Long likeCount;
    /** 현재 사용자의 좋아요 여부 */
    private Boolean likedByMe;
    
    /** 애니메이션 제목 */
    private String animeTitle;
    /** 애니메이션 타입 (OP/ED 등) */
    private AnimeType animeType;

    /**
     * Song 엔티티로부터 SongDTO 생성
     * 
     * @param song Song 엔티티
     * @return SongDTO 객체
     */
    public static SongDTO from(Song song) {
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
                .animeTitle(song.getAnime() != null ? song.getAnime().getTitle() : null)
                .animeType(song.getAnimeType())
                .build();
    }

    /**
     * SongDTO를 Song 엔티티로 변환
     * 
     * @return Song 엔티티
     */
    public Song toEntity() {
        return Song.builder()
                .songId(this.songId)
                .tj_number(this.tj_number)
                .ky_number(this.ky_number)
                .title_kr(this.title_kr)
                .title_en(this.title_en)
                .title_en_kr(this.title_en_kr)
                .title_jp(this.title_jp)
                .title_yomi(this.title_yomi)
                .title_yomi_kr(this.title_yomi_kr)
                .lang(this.lang)
                .genre(this.genre)
                .mood(this.mood)
                .artist(this.artist)
                .artist_kr(this.artist_kr)
                .lyrics_original(this.lyrics_original)
                .lyrics_yomi(this.lyrics_yomi)
                .lyrics_kr(this.lyrics_kr)
                .build();
    }

    /**
     * SongDTO를 Elasticsearch SongDocument로 변환
     * 
     * @return SongDocument 객체
     */
    public SongDocument toDocument() {
        return SongDocument.builder()
                .songId(this.songId)
                .tj_number(this.tj_number)
                .ky_number(this.ky_number)
                .title_kr(this.title_kr)
                .title_en(this.title_en)
                .title_en_kr(this.title_en_kr)
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
