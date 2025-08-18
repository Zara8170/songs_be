package com.example.song_be.domain.anime.dto;

import com.example.song_be.domain.anime.entity.Anime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnimeResponseDTO {
    private Long animeId;
    private String title;
    private Long songCount;

    public static AnimeResponseDTO fromEntity(Anime anime) {
        return AnimeResponseDTO.builder()
                .animeId(anime.getAnimeId())
                .title(anime.getTitle())
                .songCount((long) anime.getSongs().size())
                .build();
    }

    public static AnimeResponseDTO fromEntityWithSongCount(Anime anime, Long songCount) {
        return AnimeResponseDTO.builder()
                .animeId(anime.getAnimeId())
                .title(anime.getTitle())
                .songCount(songCount)
                .build();
    }
}