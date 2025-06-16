package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongMigrationService {

    private final SongRepository songRepository;
    private final SongDocumentService songDocumentService;

    public int migrateAllSongsToElasticsearch() {
        List<Song> songs = songRepository.findAll();

        List<SongDocument> documents = songs.stream()
                .map(SongDocument::fromEntity)
                .collect(Collectors.toList());

        songDocumentService.saveAll(documents);
        return documents.size();
    }
}
