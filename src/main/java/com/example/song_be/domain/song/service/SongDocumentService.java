package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.document.SongDocument;

import java.util.Optional;

public interface SongDocumentService {

    Iterable<SongDocument> findAll();

    Optional<SongDocument> findById(Long id);

    SongDocument save(SongDocument document);

    void deleteById(Long id);
}
