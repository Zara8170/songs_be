//package com.example.song_be.domain.like.entity;
//
//import com.example.song_be.domain.member.entity.Member;
//import com.example.song_be.domain.song.entity.Song;
//import com.example.song_be.entity.BaseEntity;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.experimental.SuperBuilder;
//
//@Getter
//@SuperBuilder
//@AllArgsConstructor
//@NoArgsConstructor
//@Setter
//@Entity
//@Table(name = "song_like")
//public class SongLike extends BaseEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "song_id")
//    private Song song;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "member_id")
//    private Member member;
//
//    public SongLike(Member member, Song song) {
//        this.member = member;
//        this.song   = song;
//    }
//}
