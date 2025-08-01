package com.example.song_be.domain.playlist.entity;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@DynamicUpdate
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "playlist")
public class Playlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id")
    private Long playlistId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<PlaylistSong> playlistSongs = new ArrayList<>();

    public void addSong(PlaylistSong playlistSong) {
        this.playlistSongs.add(playlistSong);
        playlistSong.setPlaylist(this);
    }

    public void removeSong(PlaylistSong playlistSong) {
        this.playlistSongs.remove(playlistSong);
        playlistSong.setPlaylist(null);
    }
}