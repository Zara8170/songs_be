package com.example.song_be.domain.suggestion.service;

import com.example.song_be.config.LogbackJsonConfig;
import com.example.song_be.config.RabbitMQConfig;
import com.example.song_be.domain.suggestion.dto.SuggestionEmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSuggestionEmail(SuggestionEmailMessage message) {
        try {
            // 로깅을 위한 MDC 설정
            MDC.put(LogbackJsonConfig.MDC_TRACE_ID, "suggestion-" + System.currentTimeMillis());
            
            log.info("Publishing suggestion email message for user: {}", message.getUserEmail());
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SUGGESTION_EXCHANGE,
                    RabbitMQConfig.ROUTING_SUGGESTION,
                    message
            );
            
            log.info("Successfully published suggestion email message");
            
        } catch (Exception e) {
            log.error("Failed to publish suggestion email message", e);
            throw new RuntimeException("건의사항 메시지 발송 중 오류가 발생했습니다.", e);
        } finally {
            MDC.remove(LogbackJsonConfig.MDC_TRACE_ID);
        }
    }
}
