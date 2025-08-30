package com.example.song_be.domain.suggestion.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.repository.MemberRepository;
import com.example.song_be.domain.suggestion.dto.SuggestionEmailMessage;
import com.example.song_be.domain.suggestion.dto.SuggestionRequestDTO;
import com.example.song_be.domain.suggestion.dto.SuggestionResponseDTO;
import com.example.song_be.domain.suggestion.entity.Suggestion;
import com.example.song_be.domain.suggestion.repository.SuggestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SuggestionServiceImpl implements SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final MemberRepository memberRepository;
    private final SuggestionPublisher suggestionPublisher;

    @Override
    public SuggestionResponseDTO submitSuggestion(String userEmail, SuggestionRequestDTO request) {
        try {
            // 사용자 조회
            Member member = memberRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

            // 건의사항 DB 저장
            Suggestion suggestion = Suggestion.builder()
                    .member(member)
                    .title(request.getTitle())
                    .content(request.getContent())
                    .build();

            suggestionRepository.save(suggestion);
            log.info("Saved suggestion to database: id={}, user={}", suggestion.getId(), userEmail);

            // 이메일 발송을 위한 메시지 큐 전송
            SuggestionEmailMessage emailMessage = SuggestionEmailMessage.from(userEmail, request);
            suggestionPublisher.publishSuggestionEmail(emailMessage);

            return SuggestionResponseDTO.success();

        } catch (EntityNotFoundException e) {
            log.error("User not found: {}", userEmail, e);
            return SuggestionResponseDTO.error("사용자 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("Failed to submit suggestion for user: {}", userEmail, e);
            return SuggestionResponseDTO.error("건의사항 접수 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
