package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.dto.SongPageDTO;
import com.example.song_be.domain.song.service.SongService;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/song")
@RequiredArgsConstructor
@Slf4j
public class SongController {

    private final SongService songService;

    @GetMapping("/list")
    public PageResponseDTO<SongDTO> getSongs( SongPageDTO pageReq) {
        log.debug("--- getSongs start ---");
        return songService.getSongList(pageReq);
    }

    @GetMapping("/{id}")
    public SongDTO getSong(@PathVariable Long id) {
        return songService.getSongById(id);
    }

    @PostMapping
    public SongDTO createSong(@RequestBody SongDTO songDTO) {
        return songService.createSong(songDTO);
    }

    @PutMapping("/{id}")
    public SongDTO updateSong(@PathVariable Long id, @RequestBody SongDTO songDTO) {
        return songService.updateSong(id, songDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
    }
}
