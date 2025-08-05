package com.example.song_be.domain.song.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
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
import java.util.stream.Stream;

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

    private static final List<String> INITIALS_WORD_FIELDS = List.of(
            "title_kr.initial^6",
            "title_yomi_kr.initial^6",
            "artist_kr.initial^6"
    );

    private static final List<String> INITIALS_JOINED_FIELDS = List.of(
        "title_kr.initial_joined^6",
        "title_yomi_kr.initial_joined^6",
        "artist_kr.initial_joined^6"
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
    public PageResponseDTO<SongDTO> findAllDTO(PageRequestDTO pageReq) throws IOException {

        int offset = (pageReq.getPage() - 1) * pageReq.getSize();

        SearchResponse<SongDocument> res = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .from(offset)
                        .size(pageReq.getSize())
                        .query(q -> q.matchAll(m -> m)),
                SongDocument.class);

        long total = res.hits().total() == null ? 0
                : res.hits().total().value();

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

    @Override
    public PageResponseDTO<SongDTO> searchByKeyword(String keyword,
                                                    SearchTarget target,
                                                    PageRequestDTO pageReq) throws IOException {

        int offset = (pageReq.getPage() - 1) * pageReq.getSize();

        List<String> baseFields = switch (target) {
            case TITLE  -> TITLE_FIELDS;
            case ARTIST -> ARTIST_FIELDS;
            case LYRICS -> LYRICS_FIELDS;
            default     -> Stream.of(TITLE_FIELDS, ARTIST_FIELDS, LYRICS_FIELDS)
                    .flatMap(Collection::stream)
                    .toList();
        };

        String q = (keyword == null) ? "" : keyword.trim();
        String qNoSpace = q.replaceAll("\\s+", "");

        boolean initialsOnly       = q.matches("^[\\u3131-\\u314E]+$");
        boolean initialsWithSpace  = q.matches("^[\\u3131-\\u314E]+(\\s+[\\u3131-\\u314E]+)+$");
        boolean isChoseongInput    = initialsOnly || initialsWithSpace;

        int codePoints = qNoSpace.codePointCount(0, qNoSpace.length());
        boolean useFuzzy = codePoints >= 4 && !isChoseongInput;

        BoolQuery boolQuery = BoolQuery.of(b -> {

            b.should(qb -> qb.multiMatch(mm -> mm
                    .query(q)
                    .fields(baseFields)
                    .type(TextQueryType.PhrasePrefix)
                    .operator(Operator.And)
                    .slop(0)
                    .boost(4.0f)
            ));

            if (useFuzzy) {
                b.should(qb -> qb.multiMatch(mm -> mm
                        .query(q)
                        .fields(baseFields)
                        .fuzziness("1")
                        .prefixLength(1)
                        .maxExpansions(50)
                        .boost(1.0f)
                ));
            }

            if (initialsOnly) {
                b.should(qb -> qb.multiMatch(mm -> mm
                        .query(q)
                        .fields(INITIALS_JOINED_FIELDS)
                        .type(TextQueryType.BestFields)
                        .operator(Operator.And)
                        .boost(4.0f)
                ));
            }

            if (initialsWithSpace) {
                b.should(qb -> qb.multiMatch(mm -> mm
                        .query(q)
                        .fields(INITIALS_WORD_FIELDS)
                        .type(TextQueryType.BestFields)
                        .operator(Operator.And)
                        .boost(4.0f)
                ));
            }

            b.minimumShouldMatch("1");
            return b;
        });

        SearchResponse<SongDocument> res = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .from(offset)
                        .size(pageReq.getSize())
                        .query(qb -> qb.bool(boolQuery))
                        .minScore(0.8)
                        .sort(SortOptions.of(so -> so
                                .score(_s -> _s.order(SortOrder.Desc))))
                , SongDocument.class);

        long total = Optional.ofNullable(res.hits().total())
                .map(t -> t.value())
                .orElse(0L);

        List<SongDTO> dtoList = res.hits().hits()
                .stream().map(Hit::source)
                .filter(Objects::nonNull)
                .map(this::toDTO)
                .toList();

        return PageResponseDTO.<SongDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageReq)
                .totalCount(total)
                .build();
    }
}
