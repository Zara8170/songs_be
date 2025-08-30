package com.example.song_be.domain.suggestion.service;

import com.example.song_be.config.LogbackJsonConfig;
import com.example.song_be.config.RabbitMQConfig;
import com.example.song_be.domain.suggestion.dto.SuggestionEmailMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionEmailConsumer {

    private final JavaMailSender mailSender;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.suggestion.admin-email:${MAIL_USERNAME}}")
    private String adminEmail;

    @Value("${app.suggestion.max-retry:3}")
    private int maxRetry;

    @RabbitListener(
            queues = RabbitMQConfig.QUEUE_SUGGESTION,
            containerFactory = "manualAckContainerFactory"
    )
    public void processSuggestionEmail(SuggestionEmailMessage message, Channel channel, Message amqpMessage) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        
        try {
            // 로깅을 위한 MDC 설정
            MDC.put(LogbackJsonConfig.MDC_TRACE_ID, "suggestion-email-" + System.currentTimeMillis());
            
            log.info("Processing suggestion email for user: {} (retry: {})", 
                    message.getUserEmail(), message.getRetryCount());

            sendSuggestionEmail(message);
            
            // 성공 시 ACK
            channel.basicAck(deliveryTag, false);
            log.info("Successfully sent suggestion email and acknowledged message");

        } catch (Exception e) {
            log.error("Failed to send suggestion email", e);
            handleFailure(message, channel, deliveryTag, e);
        } finally {
            MDC.remove(LogbackJsonConfig.MDC_TRACE_ID);
        }
    }

    private void sendSuggestionEmail(SuggestionEmailMessage message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(adminEmail);
        mailMessage.setSubject("[건의사항] " + message.getTitle());
        mailMessage.setText(buildEmailContent(message));
        mailMessage.setFrom(adminEmail); // 발신자를 관리자 이메일로 설정
        
        mailSender.send(mailMessage);
        log.info("Email sent successfully to admin: {}", adminEmail);
    }

    private String buildEmailContent(SuggestionEmailMessage message) {
        StringBuilder content = new StringBuilder();
        content.append("새로운 건의사항이 접수되었습니다.\n\n");
        content.append("제출자: ").append(message.getUserEmail()).append("\n");
        content.append("제목: ").append(message.getTitle()).append("\n");
        content.append("제출일시: ").append(message.getSubmittedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        content.append("내용:\n");
        content.append(message.getContent()).append("\n\n");
        content.append("---\n");
        content.append("이 메시지는 자동으로 발송되었습니다.");
        
        return content.toString();
    }

    private void handleFailure(SuggestionEmailMessage message, Channel channel, long deliveryTag, Exception e) {
        try {
            if (message.getRetryCount() < maxRetry) {
                // 재시도 큐로 전송
                SuggestionEmailMessage retryMessage = message.incrementRetry();
                
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SUGGESTION_DLX,
                        RabbitMQConfig.ROUTING_SUGGESTION_RETRY,
                        retryMessage
                );
                
                log.warn("Sent message to retry queue (attempt: {})", retryMessage.getRetryCount());
                channel.basicAck(deliveryTag, false);
                
            } else {
                // 최대 재시도 횟수 초과 - DLQ로 전송
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SUGGESTION_DLX,
                        RabbitMQConfig.ROUTING_SUGGESTION_DLQ,
                        message
                );
                
                log.error("Max retry exceeded. Sent message to DLQ for user: {}", message.getUserEmail());
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception retryException) {
            log.error("Failed to handle retry/DLQ logic", retryException);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackException) {
                log.error("Failed to NACK message", nackException);
            }
        }
    }
}
