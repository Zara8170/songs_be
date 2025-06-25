package com.example.song_be.domain.song.repository.querydsl;

import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.dto.PageRequestDTO;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.song_be.domain.song.entity.QSong.song;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SongRepositoryImpl implements SongRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Song> findListBy(PageRequestDTO requestDTO) {

        /* 1) Pageable 생성 */
        Pageable pageable = PageRequest.of(
                requestDTO.getPage() - 1,                // 0-based
                requestDTO.getSize(),
                "asc".equalsIgnoreCase(requestDTO.getSort())
                        ? Sort.by("songId").ascending()
                        : Sort.by("songId").descending()
        );

        /* 2) Sort → OrderSpecifier 변환 */
        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());

        /* 3) 실제 목록 조회 */
        List<Song> list = queryFactory
                .selectFrom(song)
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /* 4) 카운트 쿼리 (필요할 때만 실행) */
        JPAQuery<Long> countQuery = queryFactory
                .select(song.count())
                .from(song);

        /* 5) Page 리턴 */
        return PageableExecutionUtils.getPage(list, pageable, countQuery::fetchOne);
    }

    /** Sort → OrderSpecifier[] 변환 */
    private OrderSpecifier<?>[] createOrderSpecifiers(Sort sort) {

        PathBuilder<Song> path = new PathBuilder<>(Song.class, "song");

        return sort.stream()
                .map(order -> order.isAscending()
                        ? path.getComparable(order.getProperty(), Comparable.class).asc()
                        : path.getComparable(order.getProperty(), Comparable.class).desc())
                .toArray(OrderSpecifier[]::new);
    }
}
