package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.service.SongDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/es/song")
@RequiredArgsConstructor
@Slf4j
public class SongSearchController {

    private final SongDocumentService songDocumentService;

    @GetMapping("/list")
    public List<SongDTO> getSongs() {
        Iterable<SongDocument> docs = songDocumentService.findAll();
        return StreamSupport.stream(docs.spliterator(), false)
                .map(this::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public SongDTO getSong(@PathVariable Long id) {
        return songDocumentService.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));
    }

    private SongDTO toDTO(SongDocument document) {
        return SongDTO.builder()
                .songId(document.getSongId())
                .tj_number(document.getTj_number())
                .ky_number(document.getKy_number())
                .title_kr(document.getTitle_kr())
                .title_en(document.getTitle_en())
                .title_jp(document.getTitle_jp())
                .title_yomi(document.getTitle_yomi())
                .lang(document.getLang())
                .artist(document.getArtist())
                .artist_kr(document.getArtist_kr())
                .lyrics_original(document.getLyrics_original())
                .lyrics_kr(document.getLyrics_kr())
                .build();
    }
}
