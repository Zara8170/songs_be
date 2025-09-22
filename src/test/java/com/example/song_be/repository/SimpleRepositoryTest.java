package com.example.song_be.repository;

import com.example.song_be.domain.song.repository.SongRepository;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.domain.anime.repository.AnimeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 간단한 리포지토리 동작 확인 테스트
 * Spring Data 모듈 충돌 없이 리포지토리들이 정상 동작하는지 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SimpleRepositoryTest {

    @Autowired
    private SongRepository songRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private AnimeRepository animeRepository;

    @Test
    @DisplayName("리포지토리들이 정상적으로 주입되고 기본 동작이 가능한지 확인")
    void repositoriesShouldBeInjectedAndWorking() {
        // Given - 리포지토리들이 정상적으로 주입되었는지 확인
        assertThat(songRepository).isNotNull();
        assertThat(memberRepository).isNotNull();
        assertThat(animeRepository).isNotNull();
        
        // When & Then - 기본 count 동작이 정상적으로 수행되는지 확인
        assertThat(songRepository.count()).isNotNull();
        assertThat(memberRepository.count()).isNotNull();
        assertThat(animeRepository.count()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 데이터 조회 시 빈 결과를 반환하는지 확인")
    void shouldReturnEmptyWhenDataNotExists() {
        // When & Then
        assertThat(songRepository.findByTj_number(99999L)).isEmpty();
        assertThat(songRepository.findByKy_number(99999L)).isEmpty();
        assertThat(memberRepository.findByEmail("nonexistent@example.com")).isEmpty();
    }
}
