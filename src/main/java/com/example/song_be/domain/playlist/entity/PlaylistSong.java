package com.example.song_be.domain.playlist.entity;

import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "playlist_song")
public class PlaylistSong extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "song_order")
    private Integer order;

    public PlaylistSong(Playlist playlist, Song song, Integer order) {
        this.playlist = playlist;
        this.song = song;
        this.order = order;
    }
}