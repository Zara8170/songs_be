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

    // ===== 검색 필드 매핑 =====

    private static final List<String> TITLE_FIELDS = List.of(
            "title_kr^6",
            "title_kr.nospace^5",
            "title_en_kr^4",
            "title_en^2",
            "title_jp^3",
            "title_yomi^3",
            "title_yomi_kr^4",
            "anime_name^5",
            "anime_name.keyword^2",
            "anime_name.nospace^4",
            "title_en.keyword^1"
    );

    private static final List<String> ARTIST_FIELDS = List.of(
            "artist^3",
            "artist_kr^5",
            "artist_kr.nospace^4",
            "artist.keyword^1"
    );

    private static final List<String> LYRICS_FIELDS = List.of(
            "lyrics_original",
            "lyrics_yomi",
            "lyrics_yomi_kr",
            "lyrics_kr"
    );

    private static final List<String> ANIME_FIELDS = List.of(
            "anime_name^5",
            "anime_name.keyword^2",
            "anime_name.nospace^4"
    );

    private static final List<String> INITIALS_WORD_FIELDS = List.of(
            "title_kr.initial^6",
            "title_yomi_kr.initial^6",
            "title_en_kr.initial^6",
            "artist_kr.initial^6",
            "anime_name.initial^6"
    );

    private static final List<String> INITIALS_JOINED_FIELDS = List.of(
            "title_kr.initial_joined^6",
            "title_yomi_kr.initial_joined^6",
            "title_en_kr.initial_joined^6",
            "artist_kr.initial_joined^6",
            "anime_name.initial_joined^6"
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

    @Override
    public PageResponseDTO<SongDTO> searchByKeyword(String keyword,
                                                    SearchTarget target,
                                                    PageRequestDTO pageReq) throws IOException {

        int offset = (pageReq.getPage() - 1) * pageReq.getSize();

        List<String> baseFields = switch (target) {
            case TITLE  -> TITLE_FIELDS;
            case ARTIST -> ARTIST_FIELDS;
            case LYRICS -> LYRICS_FIELDS;
            case ANIME  -> ANIME_FIELDS;
            default     -> Stream.of(TITLE_FIELDS, ARTIST_FIELDS, LYRICS_FIELDS, ANIME_FIELDS)
                    .flatMap(Collection::stream)
                    .toList();
        };

        String q = (keyword == null) ? "" : keyword.trim();

        boolean quoted = q.length() > 1 && q.startsWith("\"") && q.endsWith("\"");
        String qStripped = quoted ? q.substring(1, q.length() - 1) : q;

        String qNoSpace = qStripped.replaceAll("\\s+", "");

        boolean initialsOnly       = qStripped.matches("^[\\u3131-\\u314E]+$");
        boolean initialsWithSpace  = qStripped.matches("^[\\u3131-\\u314E]+(\\s+[\\u3131-\\u314E]+)+$");
        boolean isChoseongInput    = initialsOnly || initialsWithSpace;

        int codePoints = qNoSpace.codePointCount(0, qNoSpace.length());
        boolean useFuzzy = codePoints >= 4 && !isChoseongInput;

        BoolQuery boolQuery = BoolQuery.of(b -> {

            // (A) 정확 매칭: 불용어 영향 없음 (*.exact)
            switch (target) {
                case TITLE -> b.should(qb -> qb.term(t -> t
                        .field("title_kr.exact").value(qStripped).boost(8.0f)));
                case ARTIST -> b.should(qb -> qb.term(t -> t
                        .field("artist_kr.exact").value(qStripped).boost(7.0f)));
                case LYRICS -> { /* 가사는 exact 생략 */ }
                case ANIME -> b.should(qb -> qb.term(t -> t
                        .field("anime_name.exact").value(qStripped).boost(7.5f)));
                default -> {
                    b.should(qb -> qb.term(t -> t.field("title_kr.exact").value(qStripped).boost(8.0f)));
                    b.should(qb -> qb.term(t -> t.field("artist_kr.exact").value(qStripped).boost(7.0f)));
                    b.should(qb -> qb.term(t -> t.field("anime_name.exact").value(qStripped).boost(7.5f)));
                }
            }

            // (B) 구절 매칭: 따옴표 검색 시 강화 (search_quote_analyzer)
            if (quoted) {
                if (target == SearchTarget.ARTIST) {
                    b.should(qb -> qb.matchPhrase(mp -> mp
                            .field("artist_kr")
                            .query(qStripped)
                            .slop(0)
                            .boost(6.0f)
                    ));
                } else if (target == SearchTarget.TITLE) {
                    b.should(qb -> qb.matchPhrase(mp -> mp
                            .field("title_kr")
                            .query(qStripped)
                            .slop(0)
                            .boost(6.0f)
                    ));
                } else if (target == SearchTarget.ANIME) {
                    b.should(qb -> qb.matchPhrase(mp -> mp
                            .field("anime_name")
                            .query(qStripped)
                            .slop(0)
                            .boost(6.0f)
                    ));
                } else {
                    b.should(qb -> qb.matchPhrase(mp -> mp
                            .field("title_kr")
                            .query(qStripped)
                            .slop(0)
                            .boost(6.0f)
                    ));
                    b.should(qb -> qb.matchPhrase(mp -> mp
                            .field("artist_kr")
                            .query(qStripped)
                            .slop(0)
                            .boost(5.0f)
                    ));
                    b.should(qb -> qb.matchPhrase(mp -> mp
                            .field("anime_name")
                            .query(qStripped)
                            .slop(0)
                            .boost(6.0f)
                    ));
                }
            }

            // (C) 일반 멀티 필드 매칭
            b.should(qb -> qb.multiMatch(mm -> mm
                    .query(qStripped)
                    .fields(baseFields)
                    .type(TextQueryType.BestFields)
                    .minimumShouldMatch("70%")
                    .operator(Operator.Or)
                    .tieBreaker(0.2)
                    .boost(4.0f)
            ));

            // (D) 공백 무시 매칭 (nospace)
            List<String> nospaceFields = switch (target) {
                case TITLE  -> List.of("title_kr.nospace^5", "title_en_kr.nospace^4", "anime_name.nospace^5");
                case ARTIST -> List.of("artist_kr.nospace^4");
                case LYRICS -> List.of();
                case ANIME  -> List.of("anime_name.nospace^5");
                default -> List.of("title_kr.nospace^5", "title_en_kr.nospace^4", "artist_kr.nospace^4", "anime_name.nospace^5");
            };
            if (!nospaceFields.isEmpty()) {
                b.should(qb -> qb.multiMatch(mm -> mm
                        .query(qNoSpace)
                        .fields(nospaceFields)
                        .type(TextQueryType.BestFields)
                        .minimumShouldMatch("70%")
                        .tieBreaker(0.2)
                        .boost(3.5f)
                ));
            }

            // (E) 오타 허용 (긴 질의에서만)
            if (useFuzzy) {
                b.should(qb -> qb.multiMatch(mm -> mm
                        .query(qStripped)
                        .fields(baseFields)
                        .fuzziness("AUTO:1,2")
                        .type(TextQueryType.BestFields)
                        .operator(Operator.Or)
                        .minimumShouldMatch("70%")
                        .prefixLength(1)
                        .maxExpansions(50)
                        .tieBreaker(0.2)
                        .boost(1.5f)
                ));
                if (!nospaceFields.isEmpty()) {
                    b.should(qb -> qb.multiMatch(mm -> mm
                            .query(qNoSpace)
                            .fields(nospaceFields)
                            .fuzziness("AUTO:1,2")
                            .type(TextQueryType.BestFields)
                            .minimumShouldMatch("70%")
                            .prefixLength(1)
                            .maxExpansions(50)
                            .tieBreaker(0.2)
                            .boost(2.0f)
                    ));
                }
            }

            // (F) 초성 — 붙여쓴 입력
            if (initialsOnly) {
                List<String> initialsFields = switch (target) {
                    case TITLE -> List.of(
                            "title_kr.initial_joined^6",
                            "title_yomi_kr.initial_joined^6",
                            "title_en_kr.initial_joined^6",
                            "anime_name.initial_joined^6"
                    );
                    case ARTIST -> List.of("artist_kr.initial_joined^6");
                    case LYRICS -> List.of();
                    case ANIME  -> List.of("anime_name.initial_joined^6");
                    default -> INITIALS_JOINED_FIELDS;
                };
                if (!initialsFields.isEmpty()) {
                    b.should(qb -> qb.multiMatch(mm -> mm
                            .query(qStripped)
                            .fields(initialsFields)
                            .type(TextQueryType.BestFields)
                            .operator(Operator.And)
                            .boost(4.0f)
                    ));
                }
            }

            // (G) 초성 — 띄어쓴 입력
            if (initialsWithSpace) {
                List<String> initialsWordFields = switch (target) {
                    case TITLE -> List.of(
                            "title_kr.initial^6",
                            "title_yomi_kr.initial^6",
                            "title_en_kr.initial^6",
                            "anime_name.initial^6"
                    );
                    case ARTIST -> List.of("artist_kr.initial^6");
                    case LYRICS -> List.of();
                    case ANIME  -> List.of("anime_name.initial^6");
                    default -> INITIALS_WORD_FIELDS;
                };
                if (!initialsWordFields.isEmpty()) {
                    b.should(qb -> qb.multiMatch(mm -> mm
                            .query(qStripped)
                            .fields(initialsWordFields)
                            .type(TextQueryType.BestFields)
                            .operator(Operator.And)
                            .boost(4.0f)
                    ));
                }
            }

            b.minimumShouldMatch("1");
            return b;
        });

        SearchResponse<SongDocument> res = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .from(offset)
                        .size(pageReq.getSize())
                        .query(qb -> qb.bool(boolQuery))
                        .sort(SortOptions.of(so -> so.score(_s -> _s.order(SortOrder.Desc)))),
                SongDocument.class);

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
