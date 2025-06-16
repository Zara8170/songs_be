package com.example.song_be.domain.song.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
public class SongDocumentServiceImpl implements SongDocumentService {

    private final ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "songs";

    @Override
    public SongDocument save(SongDocument document) {
        try {
            elasticsearchClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(document.getSongId()))
                    .document(document)
            );
            return document;
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 저장 실패", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            elasticsearchClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(String.valueOf(id))
            );
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 삭제 실패", e);
        }
    }

    @Override
    public Optional<SongDocument> findById(Long id) {
        try {
            var response = elasticsearchClient.get(g -> g
                            .index(INDEX_NAME)
                            .id(String.valueOf(id)),
                    SongDocument.class
            );
            return Optional.ofNullable(response.source());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<SongDocument> findAll() {
        try {
            SearchResponse<SongDocument> response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q.matchAll(m -> m))
                            .size(1000), // 최대 개수 제한 필요
                    SongDocument.class
            );
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 전체 조회 실패", e);
        }
    }

    @Override
    public List<SongDocument> saveAll(List<SongDocument> docs) {
        return docs.stream().map(this::save).toList();
    }

    @Override
    public List<SongDTO> findAllDTO() {
        return StreamSupport.stream(findAll().spliterator(), false)
                .map(this::toDTO)
                .toList();
    }

    @Override
    public SongDTO findDTOById(Long id) {
        return findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));
    }

    @Override
    public List<SongDTO> searchByKeyword(String keyword) throws IOException {
        SearchResponse<SongDocument> response = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q.multiMatch(m -> m
                                .query(keyword)
                                .fields("title_kr", "artist", "lyrics_kr")
                        )),
                SongDocument.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SongDTO toDTO(SongDocument document) {
        return SongDTO.builder()
                .songId(document.getSongId())
                .tj_number(document.getTj_number())
                .ky_number(document.getKy_number())
                .title_kr(document.getTitle_kr())
                .title_en(document.getTitle_en())
                .title_jp(document.getTitle_jp())
                .title_yomi(document.getTitle_yomi())
                .lang(document.getLang())
                .artist(document.getArtist())
                .artist_kr(document.getArtist_kr())
                .lyrics_original(document.getLyrics_original())
                .lyrics_kr(document.getLyrics_kr())
                .build();
    }
}
