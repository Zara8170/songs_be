package com.example.song_be.domain.song.controller;

import com.example.song_be.domain.song.dto.SongDTO;
import com.example.song_be.domain.song.service.SongService;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;
import com.example.song_be.security.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/song")
@RequiredArgsConstructor
@Slf4j
public class SongController {

    private final SongService songService;

    @GetMapping("/list")
    public PageResponseDTO<SongDTO> getSongs(PageRequestDTO pageReq, Authentication authentication) {
        log.debug("--- getSongs start ---");
        Long memberId = null;
        if (authentication != null && authentication.getPrincipal() instanceof MemberDTO) {
            memberId = ((MemberDTO) authentication.getPrincipal()).getId();
        }
        log.info("Request for /api/song/list with memberId: {}", memberId);
        PageResponseDTO<SongDTO> response = songService.getSongList(pageReq, memberId);

        log.info("Response for /api/song/list: {}", response);

        return response;
    }

    @GetMapping("/{id}")
    public SongDTO getSong(@PathVariable Long id) {
        return songService.getSongById(id);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<SongDTO>> getSongsByIds(@RequestBody List<Long> songIds) {
        List<SongDTO> songs = songService.findSongsByIds(songIds);
        return ResponseEntity.ok(songs);
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
