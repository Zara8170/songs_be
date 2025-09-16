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

/**
 * 곡 검색 API 컨트롤러 (Elasticsearch 기반)
 * Elasticsearch를 통한 곡 검색 및 색인 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/es/song")
@RequiredArgsConstructor
@Slf4j
public class SongSearchController {

    private final SongDocumentService songDocumentService;

    /**
     * 전체 곡 목록 조회 (페이징)
     * 
     * @param pageReq 페이징 요청 정보
     * @return 페이징된 곡 목록
     * @throws IOException Elasticsearch 통신 오류
     */
    @GetMapping("/list")
    public PageResponseDTO<SongDTO> getSongs(@ModelAttribute PageRequestDTO pageReq) throws IOException {
        return songDocumentService.findAllDTO(pageReq);
    }

    /**
     * 특정 곡 상세 조회
     * 
     * @param id 조회할 곡의 ID
     * @return 곡 상세 정보
     */
    @GetMapping("/{id}")
    public SongDTO getSong(@PathVariable Long id) {
        return songDocumentService.findDTOById(id);
    }

    /**
     * 곡 검색
     * 
     * @param keyword 검색 키워드
     * @param target 검색 대상 (제목, 아티스트, 애니메이션 등)
     * @param pageReq 페이징 요청 정보
     * @return 검색된 곡 목록
     * @throws IOException Elasticsearch 통신 오류
     */
    @GetMapping("/search")
    public PageResponseDTO<SongDTO> searchSongs(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "ALL") SearchTarget target,
                                                @ModelAttribute PageRequestDTO pageReq) throws IOException {
        return songDocumentService.searchByKeyword(keyword, target, pageReq);
    }

    /**
     * 곡을 Elasticsearch에 저장
     * 
     * @param songDTO 저장할 곡 정보
     * @return 저장된 곡 정보
     */
    @PostMapping
    public SongDTO saveSong(@RequestBody SongDTO songDTO) {
        SongDocument document = songDTO.toDocument();
        SongDocument savedDocument = songDocumentService.save(document);
        return songDocumentService.toDTO(savedDocument);
    }

    /**
     * Elasticsearch에서 곡 삭제
     * 
     * @param id 삭제할 곡의 ID
     */
    @DeleteMapping("/{id}")
    public void deleteSong(@PathVariable Long id) {
        songDocumentService.deleteById(id);
    }


}

