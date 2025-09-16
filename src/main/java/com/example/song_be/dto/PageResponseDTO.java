package com.example.song_be.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 페이징 응답 데이터 전송 객체
 * 
 * @param <E> 응답 데이터 타입
 */
@Data
public class PageResponseDTO<E> {

    /** 현재 페이지의 데이터 목록 */
    private List<E> dtoList;

    /** 페이지 번호 목록 */
    private List<Integer> pageNumList;

    /** 페이징 요청 정보 */
    private PageRequestDTO pageRequestDTO;

    /** 이전/다음 페이지 존재 여부 */
    private boolean prev, next;

    /** 전체 개수, 이전/다음 페이지 번호, 전체 페이지 수, 현재 페이지 */
    private int totalCount, prevPage, nextPage, totalPage, current;

    /**
     * 페이징 응답 객체 생성자
     * 
     * @param dtoList 현재 페이지 데이터 목록
     * @param pageRequestDTO 페이징 요청 정보
     * @param totalCount 전체 데이터 개수
     */
    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(List<E> dtoList, PageRequestDTO pageRequestDTO, long totalCount) {

        this.dtoList = dtoList;
        this.pageRequestDTO = pageRequestDTO;
        this.totalCount = (int) totalCount;

        int last = (int) Math.ceil(totalCount / (double) pageRequestDTO.getSize());

        // prev‧next 플래그 계산을 "현재 페이지" 기준으로 단순화
        this.prev = pageRequestDTO.getPage() > 1;
        this.next = pageRequestDTO.getPage() < last;

        if (prev) this.prevPage = pageRequestDTO.getPage() - 1;
        if (next) this.nextPage = pageRequestDTO.getPage() + 1;

        // 페이지 버튼(1 ~ last) 목록
        this.pageNumList = IntStream
                .rangeClosed(1, last)
                .boxed()
                .collect(Collectors.toList());

        this.totalPage = last;
        this.current = pageRequestDTO.getPage();
    }

}
