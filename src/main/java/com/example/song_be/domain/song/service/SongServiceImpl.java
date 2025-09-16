package com.example.song_be.domain.song.service;

import com.example.song_be.domain.anime.entity.Anime;
import com.example.song_be.domain.anime.repository.AnimeRepository;
import com.example.song_be.domain.like.repository.SongLikeRepository;
import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.SongRepository;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 곡 관련 비즈니스 로직을 처리하는 서비스 구현체
 * 곡의 CRUD, 좋아요 정보 포함 조회, 애니메이션 연동 등을 담당합니다.
 */
@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final MemberRepository memberRepository;
    private final SongLikeRepository songLikeRepository;
    private final AnimeRepository animeRepository;

    /**
     * 곡 목록 조회 (페이징 지원, 회원별 좋아요 정보 포함)
     * 
     * @param requestDTO 페이징 요청 정보
     * @param memberId 회원 ID (nullable, 비로그인 사용자 지원)
     * @return 페이징된 곡 목록과 좋아요 정보
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SongDTO> getSongList(PageRequestDTO requestDTO, Long memberId) {
        List<Long> likedSongIds = Collections.emptyList();
        if (memberId != null) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            likedSongIds = songLikeRepository.findByMember(member).stream()
                    .map(like -> like.getSong().getSongId())
                    .toList();
        }

        var page = songRepository.findListBy(requestDTO);

        final List<Long> finalLikedSongIds = likedSongIds;
        List<SongDTO> dtoList = page.getContent()
                .stream()
                .map(song -> {
                    SongDTO dto = SongDTO.from(song);
                    dto.setLikedByMe(finalLikedSongIds.contains(song.getSongId()));
                    return dto;
                })
                .toList();

        return PageResponseDTO.<SongDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(requestDTO)
                .totalCount(page.getTotalElements())
                .build();
    }

    /**
     * 특정 곡 상세 조회
     * 
     * @param id 곡 ID
     * @return 곡 상세 정보
     * @throws IllegalArgumentException 곡이 존재하지 않는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public SongDTO getSongById(Long id) {
        log.info("getSongById start...");

        return songRepository.findById(id)
                .map(SongDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));
    }

    /**
     * 여러 곡 ID로 곡 목록 조회
     * 
     * @param songIds 곡 ID 목록
     * @return 곡 목록
     */
    @Override
    public List<SongDTO> findSongsByIds(List<Long> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Song> songs = songRepository.findAllById(songIds);
        return songs.stream()
                .map(SongDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 새 곡 등록
     * 노래방 번호 중복 검사, 애니메이션 자동 생성/연결 처리
     * 
     * @param songDTO 등록할 곡 정보
     * @return 등록된 곡 정보
     * @throws IllegalArgumentException 중복된 노래방 번호가 있는 경우
     */
    @Override
    public SongDTO createSong(SongDTO songDTO) {
        log.info("createSong start...");

        if (songDTO.getTj_number() != null) {
            songRepository.findByTj_number(songDTO.getTj_number())
                    .ifPresent(existingSong -> {
                        throw new IllegalArgumentException("이미 등록된 곡입니다. (TJ 번호: " + songDTO.getTj_number() + ")");
                    });
        }

        if (songDTO.getKy_number() != null) {
            songRepository.findByKy_number(songDTO.getKy_number())
                    .ifPresent(existingSong -> {
                        throw new IllegalArgumentException("이미 등록된 곡입니다. (KY 번호: " + songDTO.getKy_number() + ")");
                    });
        }

        // 애니메이션 처리
        Anime anime = null;
        if (songDTO.getAnimeTitle() != null && !songDTO.getAnimeTitle().trim().isEmpty()) {
            anime = animeRepository.findAllOrderByTitle().stream()
                    .filter(a -> a.getTitle().equals(songDTO.getAnimeTitle()))
                    .findFirst()
                    .orElseGet(() -> {
                        // 새로운 애니메이션 생성
                        Anime newAnime = Anime.builder()
                                .title(songDTO.getAnimeTitle())
                                .build();
                        return animeRepository.save(newAnime);
                    });
        }

        // Song 엔티티 생성
        Song song = songDTO.toEntity();
        song.setAnime(anime);
        song.setAnimeType(songDTO.getAnimeType());

        Song saved = songRepository.save(song);
        return SongDTO.from(saved);
    }

    /**
     * 곡 정보 수정
     * 
     * @param id 수정할 곡 ID
     * @param songDTO 수정할 곡 정보
     * @return 수정된 곡 정보
     * @throws IllegalArgumentException 곡이 존재하지 않는 경우
     */
    @Override
    public SongDTO updateSong(Long id, SongDTO songDTO) {
        log.info("updateSong start...");
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));

        song.setTitle_kr(songDTO.getTitle_kr());
        song.setTitle_en(songDTO.getTitle_en());
        song.setTitle_jp(songDTO.getTitle_jp());
        song.setTitle_yomi(songDTO.getTitle_yomi());
        song.setTitle_yomi_kr(songDTO.getTitle_yomi_kr());
        song.setLang(songDTO.getLang());
        song.setArtist(songDTO.getArtist());
        song.setArtist_kr(songDTO.getArtist_kr());
        song.setLyrics_original(songDTO.getLyrics_original());
        song.setLyrics_kr(songDTO.getLyrics_kr());
        songRepository.save(song);
        return SongDTO.from(song);
    }

    /**
     * 곡 삭제
     * 
     * @param id 삭제할 곡 ID
     */
    @Override
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }
}
