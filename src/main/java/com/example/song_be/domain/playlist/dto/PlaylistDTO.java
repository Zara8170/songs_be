package com.example.song_be.domain.playlist.dto;

import com.example.song_be.domain.playlist.entity.Playlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistDTO {
    private Long playlistId;
    private String title;
    private String description;
    private Boolean isPublic;
    private Long memberId;
    private String memberEmail;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<PlaylistSongDTO> songs;
    private Integer songCount;

    public static PlaylistDTO from(Playlist playlist) {
        return PlaylistDTO.builder()
                .playlistId(playlist.getPlaylistId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.getIsPublic())
                .memberId(playlist.getMember().getId())
                .memberEmail(playlist.getMember().getEmail())
                .createdAt(playlist.getCreatedAt())
                .modifiedAt(playlist.getModifiedAt())
                .songs(playlist.getPlaylistSongs().stream()
                        .map(PlaylistSongDTO::from)
                        .collect(Collectors.toList()))
                .songCount(playlist.getPlaylistSongs().size())
                .build();
    }

    public static PlaylistDTO fromWithoutSongs(Playlist playlist) {
        return PlaylistDTO.builder()
                .playlistId(playlist.getPlaylistId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.getIsPublic())
                .memberId(playlist.getMember().getId())
                .memberEmail(playlist.getMember().getEmail())
                .createdAt(playlist.getCreatedAt())
                .modifiedAt(playlist.getModifiedAt())
                .songCount(playlist.getPlaylistSongs().size())
                .build();
    }
}