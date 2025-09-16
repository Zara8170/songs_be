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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 곡 관리 API 컨트롤러
 * 곡의 CRUD 및 조회 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/song")
@RequiredArgsConstructor
@Slf4j
public class SongController {

    private final SongService songService;

    /**
     * 곡 목록 조회 (페이징)
     * 
     * @param pageReq 페이징 요청 정보 (페이지 번호, 크기 등)
     * @param authentication 인증 정보 (회원별 좋아요 정보 포함)
     * @return 페이징된 곡 목록과 메타데이터
     */
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

    /**
     * 특정 곡 상세 조회
     * 
     * @param id 조회할 곡의 ID
     * @return 곡 상세 정보
     */
    @GetMapping("/{id}")
    public SongDTO getSong(@PathVariable Long id) {
        return songService.getSongById(id);
    }

    /**
     * 여러 곡 일괄 조회
     * 
     * @param songIds 조회할 곡 ID 목록
     * @return 곡 목록
     */
    @PostMapping("/batch")
    public ResponseEntity<List<SongDTO>> getSongsByIds(@RequestBody List<Long> songIds) {
        List<SongDTO> songs = songService.findSongsByIds(songIds);
        return ResponseEntity.ok(songs);
    }

    /**
     * 새 곡 등록
     * 
     * @param songDTO 등록할 곡 정보
     * @return 등록된 곡 정보
     */
    @PostMapping
    public SongDTO createSong(@RequestBody SongDTO songDTO) {
        return songService.createSong(songDTO);
    }

    /**
     * 곡 정보 수정
     * 
     * @param id 수정할 곡의 ID
     * @param songDTO 수정할 곡 정보
     * @return 수정된 곡 정보
     */
    @PutMapping("/{id}")
    public SongDTO updateSong(@PathVariable Long id, @RequestBody SongDTO songDTO) {
        return songService.updateSong(id, songDTO);
    }

    /**
     * 곡 삭제
     * 
     * @param id 삭제할 곡의 ID
     */
    @DeleteMapping("/{id}")
    public void deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
    }
}
