package com.example.song_be.config;

import com.example.song_be.domain.song.repository.SongRepository;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.domain.anime.repository.AnimeRepository;
import com.example.song_be.domain.playlist.repository.PlaylistRepository;
import com.example.song_be.domain.playlist.repository.PlaylistSongRepository;
import com.example.song_be.domain.like.repository.SongLikeRepository;
import com.example.song_be.security.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Data JPA 리포지토리 설정이 올바르게 구성되었는지 확인하는 테스트
 * JDBC 및 Redis 리포지토리 충돌 문제가 해결되었는지 검증
 */
@SpringBootTest
@ActiveProfiles("test")
class RepositoryConfigurationTest {

    @Autowired
    private SongRepository songRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private AnimeRepository animeRepository;
    
    @Autowired
    private PlaylistRepository playlistRepository;
    
    @Autowired
    private PlaylistSongRepository playlistSongRepository;
    
    @Autowired
    private SongLikeRepository songLikeRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void allJpaRepositoriesShouldBeProperlyLoaded() {
        // JPA 리포지토리들이 모두 정상적으로 로드되었는지 확인
        assertThat(songRepository).isNotNull();
        assertThat(memberRepository).isNotNull();
        assertThat(animeRepository).isNotNull();
        assertThat(playlistRepository).isNotNull();
        assertThat(playlistSongRepository).isNotNull();
        assertThat(songLikeRepository).isNotNull();
        assertThat(refreshTokenRepository).isNotNull();
    }

    @Test
    void repositoriesShouldBeJpaRepositoryInstances() {
        // 모든 리포지토리가 JpaRepository 인터페이스를 구현하고 있는지 확인
        assertThat(songRepository).isInstanceOf(JpaRepository.class);
        assertThat(memberRepository).isInstanceOf(JpaRepository.class);
        assertThat(animeRepository).isInstanceOf(JpaRepository.class);
        assertThat(playlistRepository).isInstanceOf(JpaRepository.class);
        assertThat(playlistSongRepository).isInstanceOf(JpaRepository.class);
        assertThat(songLikeRepository).isInstanceOf(JpaRepository.class);
        assertThat(refreshTokenRepository).isInstanceOf(JpaRepository.class);
    }

    @Test
    void repositoriesShouldHaveBasicCrudOperations() {
        // 기본 CRUD 메서드들이 존재하는지 확인
        assertThat(songRepository.count()).isNotNull();
        assertThat(memberRepository.count()).isNotNull();
        assertThat(animeRepository.count()).isNotNull();
        assertThat(playlistRepository.count()).isNotNull();
        assertThat(playlistSongRepository.count()).isNotNull();
        assertThat(songLikeRepository.count()).isNotNull();
        assertThat(refreshTokenRepository.count()).isNotNull();
    }
}
