package com.example.song_be.security.repository;

import com.example.song_be.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByAccessToken(String accessToken);

    Optional<RefreshToken> findByTempId(String tempId);

    void deleteByTempId(String tempId);
}
