package com.example.song_be.domain.suggestion.controller;

import com.example.song_be.domain.suggestion.dto.SuggestionRequestDTO;
import com.example.song_be.domain.suggestion.dto.SuggestionResponseDTO;
import com.example.song_be.domain.suggestion.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Suggestion", description = "건의사항 API")
@Slf4j
@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @Operation(
            summary = "건의사항 제출",
            description = "사용자가 건의사항을 제출합니다. 제목과 내용이 관리자 이메일로 전송됩니다."
    )
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SuggestionResponseDTO> submitSuggestion(
            @Parameter(description = "건의사항 정보", required = true)
            @Valid @RequestBody SuggestionRequestDTO request,
            Principal principal
    ) {
        log.info("Received suggestion submission from user: {}", principal.getName());
        
        SuggestionResponseDTO response = suggestionService.submitSuggestion(
                principal.getName(), 
                request
        );
        
        if (response.isSuccess()) {
            log.info("Successfully processed suggestion from user: {}", principal.getName());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Failed to process suggestion from user: {} - {}", 
                    principal.getName(), response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
