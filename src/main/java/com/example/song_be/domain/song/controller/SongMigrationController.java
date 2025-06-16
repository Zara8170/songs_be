package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.service.SongMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class SongMigrationController {

    private final SongMigrationService songMigrationService;

    @PostMapping("/songs")
    public String migrateSongs() {
        int count = songMigrationService.migrateAllSongsToElasticsearch();
        return count + "곡이 Elasticsearch로 마이그레이션되었습니다.";
    }
}
