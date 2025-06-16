package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.repository.SongSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SongDocumentServiceImpl implements SongDocumentService {

    private final SongSearchRepository songSearchRepository;

    @Override
    @Transactional(readOnly = true)
    public Iterable<SongDocument> findAll() {
        return songSearchRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SongDocument> findById(Long id) {
        return songSearchRepository.findById(id);
    }

    @Override
    public SongDocument save(SongDocument document) {
        return songSearchRepository.save(document);
    }

    @Override
    public void deleteById(Long id) {
        songSearchRepository.deleteById(id);
    }
}
