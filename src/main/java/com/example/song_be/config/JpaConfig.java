package com.example.song_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EnableJpaRepositories(
    basePackages = {
        "com.example.song_be.domain.*.repository",
        "com.example.song_be.security.repository"
    }
)
@Configuration
public class JpaConfig {
}
