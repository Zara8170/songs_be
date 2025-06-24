package com.example.song_be.domain.like.repository;

import com.example.song_be.domain.like.entity.SongLike;
import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {

    Optional<SongLike> findByMemberAndSong(Member member, Song song);

    List<SongLike> findAllBySong(Song song);

    List<SongLike> findAllByMember(Member member);
}
