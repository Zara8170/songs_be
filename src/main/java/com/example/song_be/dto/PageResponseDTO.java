package com.example.song_be.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PageResponseDTO<E> {

    private List<E> dtoList;

    private List<Integer> pageNumList;

    private PageRequestDTO pageRequestDTO;

    private boolean prev, next;

    private int totalCount, prevPage, nextPage, totalPage, current;

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
