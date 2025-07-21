//package com.example.song_be.domain.like.service;
//
//import com.example.song_be.domain.like.dto.SongLikeDTO;
//import com.example.song_be.domain.like.entity.SongLike;
//import com.example.song_be.domain.like.repository.SongLikeRepository;
//import com.example.song_be.domain.member.entity.Member;
//import com.example.song_be.domain.member.repository.MemberRepository;
//import com.example.song_be.domain.song.entity.Song;
//import com.example.song_be.domain.song.repository.SongRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//public class SongLIkeServiceImpl implements SongLikeService{
//
//    private final SongLikeRepository songLikeRepository;
//    private final SongRepository songRepository;
//    private final MemberRepository memberRepository;
//
//
//    @Override
//    public SongLikeDTO addLike(Long memberId, Long songId) {
//        log.debug("addLike(memberId={}, songId={})", memberId, songId);
//
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
//        Song song = songRepository.findById(songId)
//                .orElseThrow(() -> new IllegalArgumentException("곡이 존재하지 않습니다."));
//
//        songLikeRepository.findByMemberAndSong(member, song).ifPresent(songLike -> {
//            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
//        });
//
//        SongLike saved = songLikeRepository.save(SongLike.of(member, song));
//        log.info("좋아요 등록 id={} (member {}, song {})", saved.getId(), memberId, songId);
//
//        return SongLikeDTO.from(saved);
//    }
//
//    @Override
//    public void removeLike(Long memberId, Long songId) {
//        log.debug("removeLike(memberId={}, songId={})", memberId, songId);
//
//        Member member = memberRepository.getReferenceById(memberId);
//        Song   song   = songRepository.getReferenceById(songId);
//
//        SongLike like = songLikeRepository.findByMemberAndSong(member, song)
//                .orElseThrow(() -> new IllegalStateException("좋아요가 존재하지 않습니다."));
//
//        songLikeRepository.delete(like);
//        log.info("좋아요 취소 id={} (member {}, song {})", like.getId(), memberId, songId);
//    }
//
//    @Override
//    public List<SongLikeDTO> getLikesBySong(Long songId) {
//        Song song = songRepository.getReferenceById(songId);
//        return songLikeRepository.findAllBySong(song).stream()
//                .map(SongLikeDTO::from)
//                .toList();
//    }
//
//    @Override
//    public List<SongLikeDTO> getLikesByMember(Long memberId) {
//        Member member = memberRepository.getReferenceById(memberId);
//        return songLikeRepository.findAllByMember(member).stream()
//                .map(SongLikeDTO::from)
//                .toList();
//    }
//}
