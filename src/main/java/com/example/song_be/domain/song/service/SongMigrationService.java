package com.example.song_be.domain.song.service;

import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongMigrationService {

    private final SongRepository songRepository;
    private final SongDocumentService songDocumentService;

    /**
     * 모든 곡을 Elasticsearch로 색인.
     * - Anime 연관을 미리 로딩해야 fromEntity()에서 anime_name을 안전하게 세팅 가능.
     * - 대량 데이터는 배치로 쪼개서 처리.
     */
    @Transactional(readOnly = true)
    public int migrateAllSongsToElasticsearch() {
        List<Song> songs = songRepository.findAll();

        List<SongDocument> documents = songs.stream()
                .map(SongDocument::fromEntity)
                .collect(Collectors.toList());

        songDocumentService.saveAll(documents);
        return documents.size();
    }
}
