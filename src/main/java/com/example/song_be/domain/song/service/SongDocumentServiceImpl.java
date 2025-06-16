package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.repository.SongSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

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
    public List<SongDocument> saveAll(List<SongDocument> docs) {
        Iterable<SongDocument> savedDocs = songSearchRepository.saveAll(docs);
        return StreamSupport.stream(savedDocs.spliterator(), false).toList();
    }

    @Override
    public void deleteById(Long id) {
        songSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongDTO> findAllDTO() {
        Iterable<SongDocument> docs = findAll();
        return StreamSupport.stream(docs.spliterator(), false)
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SongDTO findDTOById(Long id) {
        return findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));
    }
}

