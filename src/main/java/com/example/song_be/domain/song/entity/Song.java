package com.example.song_be.domain.song.entity;

import com.example.song_be.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

@DynamicUpdate
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "song")
public class Song extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Lob
    @Column(columnDefinition = "TEXT")
    private String lyrics_original;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String lyrics_yomi;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String lyrics_kr;
}
