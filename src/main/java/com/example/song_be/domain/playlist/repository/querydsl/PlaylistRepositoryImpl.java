package com.example.song_be.domain.playlist.repository.querydsl;

import com.example.song_be.domain.playlist.entity.Playlist;
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

// import static com.example.song_be.domain.playlist.entity.QPlaylist.playlist;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PlaylistRepositoryImpl implements PlaylistRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Playlist> findByMemberIdWithPaging(Long memberId, PageRequestDTO requestDTO) {
        log.info("QueryDSL: Finding playlists for member {} with paging", memberId);

        Pageable pageable = PageRequest.of(
                requestDTO.getPage() - 1,
                requestDTO.getSize(),
                "asc".equalsIgnoreCase(requestDTO.getSort())
                        ? Sort.by("createdAt").ascending()
                        : Sort.by("createdAt").descending()
        );

        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());
        
        PathBuilder<Playlist> playlistPath = new PathBuilder<>(Playlist.class, "playlist");

        List<Playlist> list = queryFactory
                .selectFrom(playlistPath)
                .where(playlistPath.get("member").get("id").eq(memberId))
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(playlistPath.count())
                .from(playlistPath)
                .where(playlistPath.get("member").get("id").eq(memberId));

        return PageableExecutionUtils.getPage(list, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Playlist> findPublicPlaylistsWithPaging(PageRequestDTO requestDTO) {
        log.info("QueryDSL: Finding public playlists with paging");

        Pageable pageable = PageRequest.of(
                requestDTO.getPage() - 1,
                requestDTO.getSize(),
                "asc".equalsIgnoreCase(requestDTO.getSort())
                        ? Sort.by("createdAt").ascending()
                        : Sort.by("createdAt").descending()
        );

        OrderSpecifier<?>[] orderSpecifiers = createOrderSpecifiers(pageable.getSort());
        
        PathBuilder<Playlist> playlistPath = new PathBuilder<>(Playlist.class, "playlist");

        List<Playlist> list = queryFactory
                .selectFrom(playlistPath)
                .where(playlistPath.getBoolean("isPublic").eq(true))
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(playlistPath.count())
                .from(playlistPath)
                .where(playlistPath.getBoolean("isPublic").eq(true));

        return PageableExecutionUtils.getPage(list, pageable, countQuery::fetchOne);
    }

    private OrderSpecifier<?>[] createOrderSpecifiers(Sort sort) {
        PathBuilder<Playlist> path = new PathBuilder<>(Playlist.class, "playlist");

        return sort.stream()
                .map(order -> order.isAscending()
                        ? path.getComparable(order.getProperty(), Comparable.class).asc()
                        : path.getComparable(order.getProperty(), Comparable.class).desc())
                .toArray(OrderSpecifier[]::new);
    }
}