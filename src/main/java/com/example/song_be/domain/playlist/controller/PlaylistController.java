package com.example.song_be.domain.playlist.controller;

import com.example.song_be.domain.playlist.dto.*;
import com.example.song_be.domain.playlist.service.PlaylistService;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;
import com.example.song_be.security.MemberDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 플레이리스트 관리 API 컨트롤러
 * 플레이리스트의 CRUD, 곡 관리, 검색 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/playlist")
@RequiredArgsConstructor
@Slf4j
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 새 플레이리스트 생성
     * 
     * @param playlistCreateDTO 생성할 플레이리스트 정보
     * @param member 인증된 회원 정보
     * @return 생성된 플레이리스트 정보
     */
    @PostMapping
    public ResponseEntity<PlaylistDTO> createPlaylist(
            @Valid @RequestBody PlaylistCreateDTO playlistCreateDTO,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Creating playlist: {}", playlistCreateDTO.getTitle());
        
        PlaylistDTO createdPlaylist = playlistService.createPlaylist(playlistCreateDTO, member.getId());
        
        return ResponseEntity.ok(createdPlaylist);
    }

    /**
     * 플레이리스트 상세 조회
     * 
     * @param playlistId 조회할 플레이리스트 ID
     * @param member 인증된 회원 정보
     * @return 플레이리스트 상세 정보
     */
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDTO> getPlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Getting playlist: {}", playlistId);
        
        PlaylistDTO playlist = playlistService.getPlaylistById(playlistId, member.getId());
        
        return ResponseEntity.ok(playlist);
    }

    /**
     * 플레이리스트 정보 수정
     * 
     * @param playlistId 수정할 플레이리스트 ID
     * @param playlistUpdateDTO 수정할 플레이리스트 정보
     * @param member 인증된 회원 정보
     * @return 수정된 플레이리스트 정보
     */
    @PutMapping("/{playlistId}")
    public ResponseEntity<PlaylistDTO> updatePlaylist(
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistUpdateDTO playlistUpdateDTO,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Updating playlist: {}", playlistId);
        
        PlaylistDTO updatedPlaylist = playlistService.updatePlaylist(playlistId, playlistUpdateDTO, member.getId());
        
        return ResponseEntity.ok(updatedPlaylist);
    }

    /**
     * 플레이리스트 삭제
     * 
     * @param playlistId 삭제할 플레이리스트 ID
     * @param member 인증된 회원 정보
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Deleting playlist: {}", playlistId);
        
        playlistService.deletePlaylist(playlistId, member.getId());
        
        return ResponseEntity.noContent().build();
    }

    /**
     * 사용자의 플레이리스트 목록 조회
     * 
     * @param member 인증된 회원 정보
     * @return 사용자의 플레이리스트 목록
     */
    @GetMapping("/my")
    public ResponseEntity<List<PlaylistDTO>> getUserPlaylists(@AuthenticationPrincipal MemberDTO member) {
        log.info("Getting user playlists");
        
        List<PlaylistDTO> playlists = playlistService.getUserPlaylists(member.getId());
        
        return ResponseEntity.ok(playlists);
    }

    /**
     * 사용자의 플레이리스트 목록 조회 (페이징)
     * 
     * @param pageRequestDTO 페이징 요청 정보
     * @param member 인증된 회원 정보
     * @return 페이징된 사용자의 플레이리스트 목록
     */
    @GetMapping("/my/paging")
    public ResponseEntity<PageResponseDTO<PlaylistDTO>> getUserPlaylistsWithPaging(
            PageRequestDTO pageRequestDTO,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Getting user playlists with paging");
        
        PageResponseDTO<PlaylistDTO> playlists = playlistService.getUserPlaylistsWithPaging(member.getId(), pageRequestDTO);
        
        return ResponseEntity.ok(playlists);
    }

    /**
     * 공개 플레이리스트 목록 조회
     * 
     * @param pageRequestDTO 페이징 요청 정보
     * @return 페이징된 공개 플레이리스트 목록
     */
    @GetMapping("/public")
    public ResponseEntity<PageResponseDTO<PlaylistDTO>> getPublicPlaylists(PageRequestDTO pageRequestDTO) {
        log.info("Getting public playlists");
        
        PageResponseDTO<PlaylistDTO> playlists = playlistService.getPublicPlaylists(pageRequestDTO);
        
        return ResponseEntity.ok(playlists);
    }

    /**
     * 플레이리스트 제목으로 검색
     * 
     * @param title 검색할 플레이리스트 제목
     * @param member 인증된 회원 정보
     * @return 검색된 플레이리스트 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<PlaylistDTO>> searchPlaylists(
            @RequestParam String title,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Searching playlists with title: {}", title);
        
        List<PlaylistDTO> playlists = playlistService.searchUserPlaylists(member.getId(), title);
        
        return ResponseEntity.ok(playlists);
    }

    /**
     * 플레이리스트에 곡 추가
     * 
     * @param playlistId 곡을 추가할 플레이리스트 ID
     * @param playlistAddSongDTO 추가할 곡 정보
     * @param member 인증된 회원 정보
     * @return 추가된 곡 정보
     */
    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<PlaylistSongDTO> addSongToPlaylist(
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistAddSongDTO playlistAddSongDTO,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Adding song {} to playlist {}", playlistAddSongDTO.getSongId(), playlistId);
        
        PlaylistSongDTO addedSong = playlistService.addSongToPlaylist(playlistId, playlistAddSongDTO, member.getId());
        
        return ResponseEntity.ok(addedSong);
    }

    /**
     * 플레이리스트에서 곡 삭제
     * 
     * @param playlistId 곡을 삭제할 플레이리스트 ID
     * @param songId 삭제할 곡 ID
     * @param member 인증된 회원 정보
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Removing song {} from playlist {}", songId, playlistId);
        
        playlistService.removeSongFromPlaylist(playlistId, songId, member.getId());
        
        return ResponseEntity.noContent().build();
    }

    /**
     * 플레이리스트의 곡 목록 조회
     * 
     * @param playlistId 곡 목록을 조회할 플레이리스트 ID
     * @param member 인증된 회원 정보
     * @return 플레이리스트의 곡 목록
     */
    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<List<PlaylistSongDTO>> getPlaylistSongs(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Getting songs for playlist: {}", playlistId);
        
        List<PlaylistSongDTO> songs = playlistService.getPlaylistSongs(playlistId, member.getId());
        
        return ResponseEntity.ok(songs);
    }

    /**
     * 플레이리스트 내 곡 순서 변경
     * 
     * @param playlistId 플레이리스트 ID
     * @param songId 순서를 변경할 곡 ID
     * @param newOrder 새로운 순서 번호
     * @param member 인증된 회원 정보
     * @return HTTP 204 No Content
     */
    @PutMapping("/{playlistId}/songs/{songId}/order")
    public ResponseEntity<Void> reorderPlaylistSongs(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            @RequestParam Integer newOrder,
            @AuthenticationPrincipal MemberDTO member) {
        log.info("Reordering song {} in playlist {} to order {}", songId, playlistId, newOrder);
        
        playlistService.reorderPlaylistSongs(playlistId, songId, newOrder, member.getId());
        
        return ResponseEntity.noContent().build();
    }


}