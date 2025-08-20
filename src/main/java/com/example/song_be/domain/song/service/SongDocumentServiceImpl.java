package com.example.song_be.domain.song.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.enums.SearchTarget;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class SongDocumentServiceImpl implements SongDocumentService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "songs";

    // ===== 검색 필드 정의 (가중치 없는 순수 필드명) =====
    private static final List<String> KR_TITLE_FIELDS = List.of("title_kr", "anime_name");
    private static final List<String> KR_ARTIST_FIELDS = List.of("artist_kr");
    private static final List<String> OTHER_TITLE_FIELDS = List.of("title_jp", "title_en");
    private static final List<String> OTHER_ARTIST_FIELDS = List.of("artist");
    private static final List<String> LYRICS_FIELDS = List.of("lyrics_kr");


    @Override
    public PageResponseDTO<SongDTO> searchByKeyword(String keyword,
                                                    SearchTarget target,
                                                    PageRequestDTO pageReq) throws IOException {

        int offset = (pageReq.getPage() - 1) * pageReq.getSize();
        String q = (keyword == null) ? "" : keyword.trim();

        // 1. 사용자 입력 분석
        boolean quoted = q.length() > 1 && q.startsWith("\"") && q.endsWith("\"");
        String qStripped = quoted ? q.substring(1, q.length() - 1) : q;

        boolean initialsOnly = qStripped.matches("^[\\u3131-\\u314E]+$");
        boolean initialsWithSpace = qStripped.matches("^[\\u3131-\\u314E\\s]+$") && !initialsOnly;
        boolean isChoseongInput = initialsOnly || initialsWithSpace;

        // 2. 검색 대상 필드 결정
        List<String> krFields;
        List<String> otherFields;

        switch (target) {
            case TITLE -> {
                krFields = KR_TITLE_FIELDS;
                otherFields = OTHER_TITLE_FIELDS;
            }
            case ARTIST -> {
                krFields = KR_ARTIST_FIELDS;
                otherFields = OTHER_ARTIST_FIELDS;
            }
            case ANIME -> {
                krFields = List.of("anime_name");
                otherFields = List.of();
            }
            case LYRICS -> {
                krFields = LYRICS_FIELDS;
                otherFields = List.of();
            }
            default -> { // ALL
                krFields = Stream.of(KR_TITLE_FIELDS, KR_ARTIST_FIELDS).flatMap(Collection::stream).toList();
                otherFields = Stream.of(OTHER_TITLE_FIELDS, OTHER_ARTIST_FIELDS).flatMap(Collection::stream).toList();
            }
        }

        // 3. Bool 쿼리 구성
        BoolQuery boolQuery = BoolQuery.of(b -> {
            if (isChoseongInput) {
                // 초성 검색일 경우, 전용 쿼리 실행
                buildChoseongQuery(b, qStripped, krFields, initialsWithSpace);
            } else {
                // 일반 검색일 경우, 다단계 점수 전략 실행
                buildGeneralQuery(b, qStripped, krFields, otherFields, quoted);
            }
            b.minimumShouldMatch("1");
            return b;
        });

        // 4. 검색 실행
        SearchResponse<SongDocument> res = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .from(offset)
                        .size(pageReq.getSize())
                        .query(qb -> qb.bool(boolQuery))
                        .sort(SortOptions.of(so -> so.score(score -> score.order(SortOrder.Desc)))),
                SongDocument.class);

        // 5. 결과 DTO로 변환 및 반환
        long total = Optional.ofNullable(res.hits().total()).map(t -> t.value()).orElse(0L);
        List<SongDTO> dtoList = res.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResponseDTO.<SongDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageReq)
                .totalCount(total)
                .build();
    }

    /**
     * 일반 검색을 위한 다단계 점수(boost)
     */
    private void buildGeneralQuery(BoolQuery.Builder b, String query, List<String> krFields, List<String> otherFields, boolean isQuoted) {
        // 단계 1: 완전 일치 (Term) - 가장 높은 점수
        for (String field : krFields) {
            b.should(q -> q.term(t -> t.field(field + ".exact").value(query).boost(10.0f)));
        }

        // 단계 2: 구문 일치 (Match Phrase) - 인용부호 검색 시 매우 높은 점수
        if (isQuoted) {
            for (String field : krFields) {
                b.should(q -> q.matchPhrase(mp -> mp.field(field).query(query).boost(8.0f)));
            }
        }

        // 단계 3: 표준 분석기 검색 (Match) - 핵심 검색
        for (String field : krFields) {
            b.should(q -> q.match(m -> m.field(field).query(query).boost(5.0f)));
        }
        for (String field : otherFields) {
            b.should(q -> q.match(m -> m.field(field).query(query).boost(3.0f)));
        }

        // 단계 4: 오타 보정 검색 (NGram) - 약간의 점수
        for (String field : krFields) {
            b.should(q -> q.match(m -> m.field(field + ".ngram").query(query).boost(2.0f)));
        }

        // 단계 5: 공백 무시 검색 (Nospace)
        for (String field : krFields) {
            b.should(q -> q.match(m -> m.field(field + ".nospace").query(query).boost(1.5f)));
        }

        // 단계 6: 한 글자 자동완성 (Autocomplete) - 가장 낮은 점수 (보험용)
        for (String field : krFields) {
            b.should(q -> q.match(m -> m.field(field + ".autocomplete").query(query).boost(0.1f)));
        }
    }

    /**
     * 초성 검색 전용 쿼리
     */
    private void buildChoseongQuery(BoolQuery.Builder b, String query, List<String> krFields, boolean hasSpace) {
        String fieldSuffix = hasSpace ? ".initial" : ".initial_joined";
        for (String field : krFields) {
            b.should(q -> q.match(m -> m
                    .field(field + fieldSuffix)
                    .query(query)
                    .operator(Operator.And) // 초성은 모든 글자가 일치해야 함
                    .boost(10.0f)
            ));
        }
    }

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
    public List<SongDocument> saveAll(List<SongDocument> docs) {
        // 대량 색인을 위해 Bulk API 사용을 권장하지만, 기존 로직을 유지합니다.
        return docs.stream().map(this::save).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        try {
            elasticsearchClient.delete(d -> d.index(INDEX_NAME).id(id.toString()));
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 삭제 실패", e);
        }
    }

    @Override
    public Optional<SongDocument> findById(Long id) {
        try {
            var res = elasticsearchClient.get(g -> g.index(INDEX_NAME).id(id.toString()), SongDocument.class);
            return Optional.ofNullable(res.source());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<SongDocument> findAll() {
        try {
            SearchResponse<SongDocument> res = elasticsearchClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.matchAll(m -> m))
                    .size(1000), SongDocument.class);
            return res.hits().hits().stream().map(Hit::source).toList();
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 전체 조회 실패", e);
        }
    }

    @Override
    public PageResponseDTO<SongDTO> findAllDTO(PageRequestDTO pageReq) throws IOException {
        int offset = (pageReq.getPage() - 1) * pageReq.getSize();

        SearchResponse<SongDocument> res = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .from(offset)
                        .size(pageReq.getSize())
                        .query(q -> q.matchAll(m -> m)),
                SongDocument.class);

        long total = res.hits().total() == null ? 0 : res.hits().total().value();

        List<SongDTO> dtoList = res.hits().hits()
                .stream().map(Hit::source)
                .map(this::toDTO)
                .toList();

        return PageResponseDTO.<SongDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageReq)
                .totalCount(total)
                .build();
    }

    @Override
    public SongDTO findDTOById(Long id) {
        return findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 곡이 없습니다: " + id));
    }
}