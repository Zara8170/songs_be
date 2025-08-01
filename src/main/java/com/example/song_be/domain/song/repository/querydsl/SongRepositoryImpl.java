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

        Pageable pageable = PageRequest.of(
                requestDTO.getPage() - 1,
                requestDTO.getSize(),
                "asc".equalsIgnoreCase(requestDTO.getSort())
                        ? Sort.by("songId").ascending()
                        : Sort.by("songId").descending()
        );

        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());

        List<Song> list = queryFactory
                .selectFrom(song)
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(song.count())
                .from(song);

        return PageableExecutionUtils.getPage(list, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(Sort sort) {

        PathBuilder<Song> path = new PathBuilder<>(Song.class, "song");

        return sort.stream()
                .map(order -> order.isAscending()
                        ? path.getComparable(order.getProperty(), Comparable.class).asc()
                        : path.getComparable(order.getProperty(), Comparable.class).desc())
                .toArray(OrderSpecifier[]::new);
    }
}
