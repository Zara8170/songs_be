package com.example.song_be.domain.playlist.dto;

import com.example.song_be.domain.playlist.entity.Playlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 플레이리스트 데이터 전송 객체
 * 플레이리스트의 기본 정보와 포함된 곡 목록을 담습니다.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistDTO {
    /** 플레이리스트 고유 ID */
    private Long playlistId;
    /** 플레이리스트 제목 */
    private String title;
    /** 플레이리스트 설명 */
    private String description;
    /** 공개 여부 */
    private Boolean isPublic;
    /** 작성자 회원 ID */
    private Long memberId;
    /** 작성자 이메일 */
    private String memberEmail;
    /** 생성 일시 */
    private LocalDateTime createdAt;
    /** 수정 일시 */
    private LocalDateTime modifiedAt;
    /** 포함된 곡 목록 */
    private List<PlaylistSongDTO> songs;
    /** 포함된 곡 개수 */
    private Integer songCount;

    /**
     * Playlist 엔티티로부터 PlaylistDTO 생성 (곡 목록 포함)
     * 
     * @param playlist Playlist 엔티티
     * @return PlaylistDTO 객체
     */
    public static PlaylistDTO from(Playlist playlist) {
        return PlaylistDTO.builder()
                .playlistId(playlist.getPlaylistId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.getIsPublic())
                .memberId(playlist.getMember().getId())
                .memberEmail(playlist.getMember().getEmail())
                .createdAt(playlist.getCreatedAt())
                .modifiedAt(playlist.getModifiedAt())
                .songs(playlist.getPlaylistSongs().stream()
                        .map(PlaylistSongDTO::from)
                        .collect(Collectors.toList()))
                .songCount(playlist.getPlaylistSongs().size())
                .build();
    }

    /**
     * Playlist 엔티티로부터 PlaylistDTO 생성 (곡 목록 제외)
     * 
     * @param playlist Playlist 엔티티
     * @return PlaylistDTO 객체 (곡 목록 없음)
     */
    public static PlaylistDTO fromWithoutSongs(Playlist playlist) {
        return PlaylistDTO.builder()
                .playlistId(playlist.getPlaylistId())
                .title(playlist.getTitle())
                .description(playlist.getDescription())
                .isPublic(playlist.getIsPublic())
                .memberId(playlist.getMember().getId())
                .memberEmail(playlist.getMember().getEmail())
                .createdAt(playlist.getCreatedAt())
                .modifiedAt(playlist.getModifiedAt())
                .songCount(playlist.getPlaylistSongs().size())
                .build();
    }
}