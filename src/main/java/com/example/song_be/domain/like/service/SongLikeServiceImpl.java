//package com.example.song_be.domain.like.service;
//
//import com.example.song_be.domain.like.dao.SongLikeDAO;
//import com.example.song_be.domain.like.dto.SongLikeCountDTO;
//import com.example.song_be.domain.like.dto.SongLikeDTO;
//import com.example.song_be.domain.like.entity.SongLike;
//import com.example.song_be.domain.like.repository.SongLikeRepository;
//import com.example.song_be.domain.member.entity.Member;
//import com.example.song_be.domain.member.repository.MemberRepository;
//import com.example.song_be.domain.song.entity.Song;
//import com.example.song_be.domain.song.repository.SongRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//public class SongLikeServiceImpl implements SongLikeService {
//
//    private final SongLikeRepository likeRepo;
//    private final SongRepository    songRepo;
//    private final MemberRepository  memberRepo;
//
//    @Override
//    @Transactional
//    public SongLikeDTO toggleLike(String tempId, Long songId) {
//
//        Member member = memberRepo.findByTempId(tempId)
//                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. tempId=" + tempId));
//        Song   song   = songRepo.getReferenceById(songId);
//
//        SongLikeDAO dao = likeRepo.findByMemberAndSong(member, song)
//                .map(like -> {                          // 이미 존재 → 삭제
//                    likeRepo.delete(like);
//                    return SongLikeDAO.unliked(songId, member.getId());
//                })
//                .orElseGet(() -> {                      // 없으면 등록
//                    SongLike saved = likeRepo.save(new SongLike(member, song));
//                    return SongLikeDAO.fromEntity(saved);
//                });
//
//        return SongLikeDTO.fromDao(dao, tempId);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<Long> getLikedSongIds(String tempId) {
//        Member member = memberRepo.findByTempId(tempId)
//                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
//        return likeRepo.findSongIdsByMember(member.getTempId());
//    }
//
//    @Override
//    public long getLikeCount(Long songId) {
//        return likeRepo.countBySongId(songId);
//    }
//
//    @Override
//    public List<SongLikeCountDTO> getLikeCounts(List<Long> ids) {
//        return likeRepo.countBySongIds(ids).stream()
//                .map(p -> new SongLikeCountDTO(p.getSongId(), p.getLikeCnt()))
//                .toList();
//    }
//}
