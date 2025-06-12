package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SongDTO> getSongList() {
        log.info("getSongList start...");

        return songRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SongDTO getSongById(Long id) {
        log.info("getSongById start...");

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));
        return toDTO(song);
    }

    @Override
    public SongDTO createSong(SongDTO songDTO) {
        log.info("createSong start...");

        Song saved = songRepository.save(toEntity(songDTO));
        return toDTO(saved);
    }

    @Override
    public SongDTO updateSong(Long id, SongDTO songDTO) {
        log.info("updateSong start...");
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));

        song.setTitle_kr(songDTO.getTitle_kr());
        song.setTitle_en(songDTO.getTitle_en());
        song.setTitle_jp(songDTO.getTitle_jp());
        song.setLang(songDTO.getLang());
        song.setArtist(songDTO.getArtist());
        song.setArtist_kr(songDTO.getArtist_kr());
        song.setLyrics_original(songDTO.getLyrics_original());
        song.setLyrics_kr(songDTO.getLyrics_kr());

        return toDTO(song);
    }

    @Override
    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }
}
