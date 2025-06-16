package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.service.SongDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
        return songDocumentService.findAllDTO();
    }

    @GetMapping("/{id}")
    public SongDTO getSong(@PathVariable Long id) {
        return songDocumentService.findDTOById(id);
    }

    @GetMapping("/search")
    public List<SongDTO> searchSongs(@RequestParam String keyword) throws IOException {
        return songDocumentService.searchByKeyword(keyword);
    }

    @PostMapping
    public SongDTO saveSong(@RequestBody SongDTO songDTO) {
        SongDocument document = songDTO.toDocument();
        SongDocument savedDocument = songDocumentService.save(document);
        return songDocumentService.toDTO(savedDocument);
    }

    @DeleteMapping("/{id}")
    public void deleteSong(@PathVariable Long id) {
        songDocumentService.deleteById(id);
    }


}

