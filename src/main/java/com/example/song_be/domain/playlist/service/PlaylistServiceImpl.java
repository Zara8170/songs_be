package com.example.song_be.domain.playlist.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.domain.playlist.dto.*;
import com.example.song_be.domain.playlist.entity.Playlist;
import com.example.song_be.domain.playlist.entity.PlaylistSong;
import com.example.song_be.domain.playlist.repository.PlaylistRepository;
import com.example.song_be.domain.playlist.repository.PlaylistSongRepository;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.domain.song.repository.SongRepository;
import com.example.song_be.dto.PageRequestDTO;
import com.example.song_be.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final MemberRepository memberRepository;
    private final SongRepository songRepository;

    @Override
    public PlaylistDTO createPlaylist(PlaylistCreateDTO playlistCreateDTO, Long memberId) {
        log.info("Creating playlist for member: {}", memberId);
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        Playlist playlist = playlistCreateDTO.toEntity(member);
        Playlist savedPlaylist = playlistRepository.save(playlist);
        
        return PlaylistDTO.fromWithoutSongs(savedPlaylist);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDTO getPlaylistById(Long playlistId, Long memberId) {
        log.info("Getting playlist: {} for member: {}", playlistId, memberId);
        
        Playlist playlist = playlistRepository.findByIdWithSongs(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));
        
        if (!playlist.getMember().getId().equals(memberId) && !playlist.getIsPublic()) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        
        return PlaylistDTO.from(playlist);
    }

    @Override
    public PlaylistDTO updatePlaylist(Long playlistId, PlaylistUpdateDTO playlistUpdateDTO, Long memberId) {
        log.info("Updating playlist: {} for member: {}", playlistId, memberId);
        
        Playlist playlist = playlistRepository.findByIdWithMember(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));
        
        if (!playlist.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        
        if (playlistUpdateDTO.getTitle() != null) {
            playlist.setTitle(playlistUpdateDTO.getTitle());
        }
        if (playlistUpdateDTO.getDescription() != null) {
            playlist.setDescription(playlistUpdateDTO.getDescription());
        }
        if (playlistUpdateDTO.getIsPublic() != null) {
            playlist.setIsPublic(playlistUpdateDTO.getIsPublic());
        }
        
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return PlaylistDTO.fromWithoutSongs(savedPlaylist);
    }

    @Override
    public void deletePlaylist(Long playlistId, Long memberId) {
        log.info("Deleting playlist: {} for member: {}", playlistId, memberId);
        
        Playlist playlist = playlistRepository.findByIdWithMember(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));
        
        if (!playlist.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        
        playlistRepository.delete(playlist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistDTO> getUserPlaylists(Long memberId) {
        log.info("Getting playlists for member: {}", memberId);
        
        List<Playlist> playlists = playlistRepository.findByMemberId(memberId);
        return playlists.stream()
                .map(PlaylistDTO::fromWithoutSongs)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PlaylistDTO> getUserPlaylistsWithPaging(Long memberId, PageRequestDTO pageRequestDTO) {
        log.info("Getting playlists with paging for member: {} using QueryDSL", memberId);
        
        Page<Playlist> playlistPage = playlistRepository.findByMemberIdWithPaging(memberId, pageRequestDTO);
        
        List<PlaylistDTO> dtoList = playlistPage.getContent().stream()
                .map(PlaylistDTO::fromWithoutSongs)
                .collect(Collectors.toList());
        
        return PageResponseDTO.<PlaylistDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(playlistPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<PlaylistDTO> getPublicPlaylists(PageRequestDTO pageRequestDTO) {
        log.info("Getting public playlists using QueryDSL");
        
        Page<Playlist> playlistPage = playlistRepository.findPublicPlaylistsWithPaging(pageRequestDTO);
        
        List<PlaylistDTO> dtoList = playlistPage.getContent().stream()
                .map(PlaylistDTO::fromWithoutSongs)
                .collect(Collectors.toList());
        
        return PageResponseDTO.<PlaylistDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(playlistPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistDTO> searchUserPlaylists(Long memberId, String title) {
        log.info("Searching playlists for member: {} with title: {}", memberId, title);
        
        List<Playlist> playlists = playlistRepository.findByMemberIdAndTitleContaining(memberId, title);
        return playlists.stream()
                .map(PlaylistDTO::fromWithoutSongs)
                .collect(Collectors.toList());
    }

    @Override
    public PlaylistSongDTO addSongToPlaylist(Long playlistId, PlaylistAddSongDTO playlistAddSongDTO, Long memberId) {
        log.info("Adding song: {} to playlist: {} for member: {}", playlistAddSongDTO.getSongId(), playlistId, memberId);
        
        Playlist playlist = playlistRepository.findByIdWithMember(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));
        
        if (!playlist.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("곡 추가 권한이 없습니다.");
        }
        
        Song song = songRepository.findById(playlistAddSongDTO.getSongId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 곡입니다."));
        
        if (playlistSongRepository.findByPlaylistIdAndSongId(playlistId, playlistAddSongDTO.getSongId()).isPresent()) {
            throw new IllegalArgumentException("이미 플레이리스트에 추가된 곡입니다.");
        }
        
        Integer order = playlistAddSongDTO.getOrder();
        if (order == null) {
            order = playlistSongRepository.findMaxOrderByPlaylistId(playlistId) + 1;
        } else {
            List<PlaylistSong> songsToReorder = playlistSongRepository.findByPlaylistIdAndOrderGreaterThan(playlistId, order - 1);
            for (PlaylistSong ps : songsToReorder) {
                ps.setOrder(ps.getOrder() + 1);
            }
            playlistSongRepository.saveAll(songsToReorder);
        }
        
        PlaylistSong playlistSong = new PlaylistSong(playlist, song, order);
        PlaylistSong savedPlaylistSong = playlistSongRepository.save(playlistSong);
        
        return PlaylistSongDTO.from(savedPlaylistSong);
    }

    @Override
    public void removeSongFromPlaylist(Long playlistId, Long songId, Long memberId) {
        log.info("Removing song: {} from playlist: {} for member: {}", songId, playlistId, memberId);
        
        Playlist playlist = playlistRepository.findByIdWithMember(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));
        
        if (!playlist.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("곡 삭제 권한이 없습니다.");
        }
        
        PlaylistSong playlistSong = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트에 해당 곡이 없습니다."));
        
        Integer removedOrder = playlistSong.getOrder();
        playlistSongRepository.delete(playlistSong);
        
        List<PlaylistSong> songsToReorder = playlistSongRepository.findByPlaylistIdAndOrderGreaterThan(playlistId, removedOrder);
        for (PlaylistSong ps : songsToReorder) {
            ps.setOrder(ps.getOrder() - 1);
        }
        playlistSongRepository.saveAll(songsToReorder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistSongDTO> getPlaylistSongs(Long playlistId, Long memberId) {
        log.info("Getting songs for playlist: {} for member: {}", playlistId, memberId);
        
        Playlist playlist = playlistRepository.findByIdWithMember(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));
        
        // 권한 체크: 본인의 플레이리스트이거나 공개 플레이리스트여야 함
        if (!playlist.getMember().getId().equals(memberId) && !playlist.getIsPublic()) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistIdOrderByOrder(playlistId);
        return playlistSongs.stream()
                .map(PlaylistSongDTO::from)
                .collect(Collectors.toList());
    }

    @Override
    public void reorderPlaylistSongs(Long playlistId, Long songId, Integer newOrder, Long memberId) {
        log.info("Reordering song: {} in playlist: {} to order: {} for member: {}", songId, playlistId, newOrder, memberId);
        
        Playlist playlist = playlistRepository.findByIdWithMember(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));
        
        if (!playlist.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("순서 변경 권한이 없습니다.");
        }
        
        PlaylistSong targetSong = playlistSongRepository.findByPlaylistIdAndSongId(playlistId, songId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트에 해당 곡이 없습니다."));
        
        Integer currentOrder = targetSong.getOrder();
        
        if (currentOrder.equals(newOrder)) {
            return;
        }
        
        List<PlaylistSong> allSongs = playlistSongRepository.findByPlaylistIdOrderByOrder(playlistId);
        
        if (newOrder < 1 || newOrder > allSongs.size()) {
            throw new IllegalArgumentException("유효하지 않은 순서입니다.");
        }
        
        if (currentOrder < newOrder) {
            for (PlaylistSong song : allSongs) {
                if (song.getOrder() > currentOrder && song.getOrder() <= newOrder) {
                    song.setOrder(song.getOrder() - 1);
                }
            }
        } else {
            for (PlaylistSong song : allSongs) {
                if (song.getOrder() >= newOrder && song.getOrder() < currentOrder) {
                    song.setOrder(song.getOrder() + 1);
                }
            }
        }
        
        targetSong.setOrder(newOrder);
        playlistSongRepository.saveAll(allSongs);
    }
}