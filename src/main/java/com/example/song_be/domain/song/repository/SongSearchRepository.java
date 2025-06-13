package com.example.song_be.domain.song.repository;

import com.example.song_be.domain.song.document.SongDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SongSearchRepository extends ElasticsearchRepository<SongDocument, Long> {
}
