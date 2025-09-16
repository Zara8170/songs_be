package com.example.song_be.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 페이징 요청 데이터 전송 객체
 * API 요청 시 페이지 번호, 크기, 정렬 정보를 포함합니다.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {

    @Schema(description = "페이지 번호(1부터 시작)",
            defaultValue = "1",
            example = "1")
    @Builder.Default
    private int page = 1;

    @Schema(description = "한 페이지 크기",
            defaultValue = "10",
            example = "10")
    @Builder.Default
    private int size = 10;


    @Schema(description = "정렬 방향",
            defaultValue = "asc",
            example = "asc")
    @Builder.Default
    private String sort = "asc";
}
