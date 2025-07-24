//package com.example.song_be.domain.like.repository;
//
//import com.example.song_be.domain.like.entity.SongLike;
//import com.example.song_be.domain.member.entity.Member;
//import com.example.song_be.domain.song.entity.Song;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface SongLikeRepository extends JpaRepository<SongLike, Long> {
//
//    Optional<SongLike> findByMemberAndSong(Member member, Song song);
//
//    @Query("select l.song.songId from SongLike l where l.member.tempId = :tempId")
//    List<Long> findSongIdsByMember(@Param("tempId") String tempId);
//
//    /* 1) 단일 곡의 좋아요 개수 */
//    @Query("select count(l) from SongLike l where l.song.songId = :songId")
//    long countBySongId(@Param("songId") Long songId);
//
//    /* 2) 여러 곡을 한 번에 – 통계/목록 화면용 */
//    @Query("""
//           select l.song.songId as songId,
//                  count(l)       as likeCnt
//             from SongLike l
//            where l.song.songId in :songIds
//            group by l.song.songId
//           """)
//    List<SongLikeCount> countBySongIds(@Param("songIds") List<Long> songIds);
//
//    interface SongLikeCount {
//        Long getSongId();
//        Long getLikeCnt();
//    }
//}
