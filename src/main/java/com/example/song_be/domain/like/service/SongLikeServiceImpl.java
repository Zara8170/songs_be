package com.example.song_be.domain.like.service;

import com.example.song_be.domain.like.dao.SongLikeDAO;
import com.example.song_be.domain.like.dto.SongLikeCountDTO;
import com.example.song_be.domain.like.dto.SongLikeDTO;
import com.example.song_be.domain.like.entity.SongLike;
import com.example.song_be.domain.like.repository.SongLikeRepository;
import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 곡 좋아요 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 좋아요 추가/제거, 좋아요한 곡 목록 조회, 좋아요 통계 등을 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SongLikeServiceImpl implements SongLikeService {

    private final SongLikeRepository likeRepo;
    private final SongRepository    songRepo;
    private final MemberRepository  memberRepo;

    /**
     * 곡 좋아요 추가/삭제 토글
     * 이미 좋아요가 있으면 삭제하고, 없으면 추가합니다.
     * 
     * @param memberId 회원 ID
     * @param songId 곡 ID
     * @return 좋아요 상태 정보
     * @throws IllegalArgumentException 회원이 존재하지 않는 경우
     */
    @Override
    @Transactional
    public SongLikeDTO toggleLike(Long memberId, Long songId) {

        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. memberId=" + memberId));
        Song   song   = songRepo.getReferenceById(songId);

        SongLikeDAO dao = likeRepo.findByMemberAndSong(member, song)
                .map(like -> {                          // 이미 존재 → 삭제
                    likeRepo.delete(like);
                    return SongLikeDAO.unliked(songId, member.getId());
                })
                .orElseGet(() -> {                      // 없으면 등록
                    SongLike saved = likeRepo.save(new SongLike(member, song));
                    return SongLikeDAO.fromEntity(saved);
                });

        return SongLikeDTO.fromDao(dao);
    }

    /**
     * 회원이 좋아요한 곡 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 좋아요한 곡 목록
     * @throws IllegalArgumentException 회원이 존재하지 않는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public List<SongDTO> getLikedSongs(Long memberId) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        List<SongDTO> songList = likeRepo.findByMemberWithSong(member).stream()
                .map(like -> SongDTO.from(like.getSong()))
                .toList();

        log.info("getLikedSongs: {}", songList);

        return songList;
    }

    /**
     * 회원이 좋아요한 곡 ID 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 좋아요한 곡 ID 목록
     * @throws IllegalArgumentException 회원이 존재하지 않는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> getLikedSongIds(Long memberId) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        List<Long> songIds = likeRepo.findByMemberWithSong(member).stream()
                .map(like -> like.getSong().getSongId())
                .toList();

        log.info("getLikedSongIds for member {}: {}", memberId, songIds);

        return songIds;
    }

    /**
     * 특정 곡의 좋아요 개수 조회
     * 
     * @param songId 곡 ID
     * @return 좋아요 개수
     */
    @Override
    public long getLikeCount(Long songId) {
        return likeRepo.countBySongId(songId);
    }

    /**
     * 여러 곡의 좋아요 개수 일괄 조회
     * 
     * @param ids 곡 ID 목록
     * @return 곡별 좋아요 개수 목록
     */
    @Override
    public List<SongLikeCountDTO> getLikeCounts(List<Long> ids) {
        return likeRepo.countBySongIds(ids).stream()
                .map(p -> new SongLikeCountDTO(p.getSongId(), p.getLikeCnt()))
                .toList();
    }

    /**
     * 좋아요가 있는 모든 곡들의 좋아요 통계 조회 (좋아요 많은 순)
     * 
     * @return 곡별 좋아요 통계 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<SongLikeCountDTO> getAllSongsWithLikes() {
        return likeRepo.findAllSongsWithLikes().stream()
                .map(p -> new SongLikeCountDTO(p.getSongId(), p.getLikeCnt()))
                .toList();
    }
}
