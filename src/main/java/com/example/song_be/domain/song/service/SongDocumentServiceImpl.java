package com.example.song_be.domain.song.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.song_be.domain.song.document.SongDocument;
import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.enums.SearchTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
public class SongDocumentServiceImpl implements SongDocumentService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "songs";

    // 검색 필드 매핑
    private static final List<String> TITLE_FIELDS = List.of(
            "title_kr^5",
            "title_en^4",
            "title_jp^4",
            "title_yomi^3",
            "title_yomi_kr^3"
    );
    private static final List<String> ARTIST_FIELDS = List.of(
            "artist^4",
            "artist_kr^4"
    );
    private static final List<String> LYRICS_FIELDS = List.of(
            "lyrics_original",
            "lyrics_yomi",
            "lyrics_yomi_kr",
            "lyrics_kr"
    );

    private static final Map<SearchTarget, List<String>> FIELD_MAP = Map.of(
            SearchTarget.TITLE,  TITLE_FIELDS,
            SearchTarget.ARTIST, ARTIST_FIELDS,
            SearchTarget.LYRICS, LYRICS_FIELDS
    );

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
        return docs.stream().map(this::save).toList();
    }

    @Override
    public void deleteById(Long id) {
        try { elasticsearchClient.delete(d -> d.index(INDEX_NAME).id(id.toString())); }
        catch (IOException e) { throw new RuntimeException("Elasticsearch 삭제 실패", e); }
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
    public List<SongDTO> searchByKeyword(String keyword, SearchTarget target) throws IOException {

        List<String> baseFields =
                (target == SearchTarget.ALL)
                ? Stream.of(TITLE_FIELDS, ARTIST_FIELDS, LYRICS_FIELDS)
                    .flatMap(List::stream)
                    .toList()
                : FIELD_MAP.get(target);

        boolean choSeongOnly = keyword.matches("^[\\u3131-\\u314E]+$");
        boolean shortQuery = keyword.codePointCount(0, keyword.length()) <= 2;

        final List<String> resolvedFields = choSeongOnly
                ? List.of("title_kr.initial^3", "title_yomi_kr.initial^3", "artist_kr.initial^3")
                : baseFields;

        SearchResponse<SongDocument> res = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q.multiMatch(m -> {
                            m.query(keyword)
                                    .fields(resolvedFields)
                                    .type(TextQueryType.BestFields)
                                    .operator(Operator.And)
                                    .minimumShouldMatch(shortQuery ? "100%" : "2<75%");
                            if (!shortQuery && !choSeongOnly) {
                                m.fuzziness("AUTO");
                            }
                            return m;
                        }))
                        .minScore(choSeongOnly ? null : (shortQuery ? 2d : 0d))
                , SongDocument.class);

        return res.hits().hits()
                .stream()
                .map(Hit::source)
                .map(this::toDTO)
                .toList();
    }


}
