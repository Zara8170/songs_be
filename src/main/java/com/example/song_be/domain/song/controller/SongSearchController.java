package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.enums.SearchTarget;
import com.example.song_be.domain.song.service.SongDocumentService;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/es/song")
@RequiredArgsConstructor
@Slf4j
public class SongSearchController {

    private final SongDocumentService songDocumentService;

    @GetMapping("/list")
    public PageResponseDTO<SongDTO> getSongs(@ModelAttribute PageRequestDTO pageReq) throws IOException {
        return songDocumentService.findAllDTO(pageReq);
    }

    @GetMapping("/{id}")
    public SongDTO getSong(@PathVariable Long id) {
        return songDocumentService.findDTOById(id);
    }

    @GetMapping("/search")
    public PageResponseDTO<SongDTO> searchSongs(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "ALL") SearchTarget target,
                                                @ModelAttribute PageRequestDTO pageReq) throws IOException {
        return songDocumentService.searchByKeyword(keyword, target, pageReq);
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

