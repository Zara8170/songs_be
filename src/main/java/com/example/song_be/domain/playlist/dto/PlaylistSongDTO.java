package com.example.song_be.domain.playlist.dto;

import com.example.song_be.domain.playlist.entity.PlaylistSong;
import com.example.song_be.domain.song.dto.SongDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistSongDTO {
    private Long id;
    private Long playlistId;
    private SongDTO song;
    private Integer order;

    public static PlaylistSongDTO from(PlaylistSong playlistSong) {
        return PlaylistSongDTO.builder()
                .id(playlistSong.getId())
                .playlistId(playlistSong.getPlaylist().getPlaylistId())
                .song(SongDTO.from(playlistSong.getSong()))
                .order(playlistSong.getOrder())
                .build();
    }
}