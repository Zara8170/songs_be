package com.example.song_be.domain.like.repository;

import com.example.song_be.domain.like.entity.SongLike;
import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {

    Optional<SongLike> findByMemberAndSong(Member member, Song song);

    List<SongLike> findByMember(Member member);

    @Query("SELECT sl FROM SongLike sl JOIN FETCH sl.song WHERE sl.member = :member")
    List<SongLike> findByMemberWithSong(@Param("member") Member member);

    /* 1) 단일 곡의 좋아요 개수 */
    @Query("select count(l) from SongLike l where l.song.songId = :songId")
    long countBySongId(@Param("songId") Long songId);

    /* 2) 여러 곡을 한 번에 – 통계/목록 화면용 */
    @Query("""
           select l.song.songId as songId,
                  count(l)       as likeCnt
             from SongLike l
            where l.song.songId in :songIds
            group by l.song.songId
           """)
    List<SongLikeCount> countBySongIds(@Param("songIds") List<Long> songIds);

    /* 3) 좋아요가 있는 모든 노래들의 카운트 조회 */
    @Query("""
           select l.song.songId as songId,
                  count(l)       as likeCnt
             from SongLike l
            group by l.song.songId
            having count(l) > 0
            order by count(l) desc
           """)
    List<SongLikeCount> findAllSongsWithLikes();

    interface SongLikeCount {
        Long getSongId();
        Long getLikeCnt();
    }
}
