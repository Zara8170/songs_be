package com.example.song_be.domain.song.service;

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
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final MemberRepository memberRepository;
    private final SongLikeRepository songLikeRepository;

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

    @Override
    @Transactional(readOnly = true)
    public SongDTO getSongById(Long id) {
        log.info("getSongById start...");

        return songRepository.findById(id)
                .map(SongDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));
    }

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

        Song saved = songRepository.save(songDTO.toEntity());
        return SongDTO.from(saved);
    }

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

    @Override
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }
}
