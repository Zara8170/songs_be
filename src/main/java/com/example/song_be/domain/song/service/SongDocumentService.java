package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.dto.SongPageDTO;
import com.example.song_be.domain.song.enums.SearchTarget;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface SongDocumentService {

    Iterable<SongDocument> findAll();

    Optional<SongDocument> findById(Long id);

    PageResponseDTO<SongDTO> searchByKeyword(String keyword,
                                             SearchTarget target,
                                             SongPageDTO pageReq) throws IOException;

    SongDocument save(SongDocument document);

    List<SongDocument> saveAll(List<SongDocument> docs);

    void deleteById(Long id);

    PageResponseDTO<SongDTO> findAllDTO(SongPageDTO pageReq) throws IOException;

    SongDTO findDTOById(Long id);

    default SongDTO toDTO(SongDocument document) {
        return SongDTO.builder()
                .songId(document.getSongId())
                .tj_number(document.getTj_number())
                .ky_number(document.getKy_number())
                .title_kr(document.getTitle_kr())
                .title_en(document.getTitle_en())
                .title_jp(document.getTitle_jp())
                .title_yomi(document.getTitle_yomi())
                .title_yomi_kr(document.getTitle_yomi_kr())
                .lang(document.getLang())
                .artist(document.getArtist())
                .artist_kr(document.getArtist_kr())
                .lyrics_original(document.getLyrics_original())
                .lyrics_kr(document.getLyrics_kr())
                .build();
    }
}

