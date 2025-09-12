package com.example.song_be.domain.song.service;

import com.example.song_be.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Dead Letter Queue의 상태를 모니터링하는 서비스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueHealthIndicator {

    private final RabbitAdmin rabbitAdmin;
    
    // DLQ에 메시지가 이 개수 이상 쌓이면 경고
    private static final int WARNING_THRESHOLD = 10;
    // DLQ에 메시지가 이 개수 이상 쌓이면 에러
    private static final int ERROR_THRESHOLD = 50;

    public Map<String, Object> getHealthStatus() {
        try {
            // DLQ 큐 정보 조회
            QueueInformation dlqInfo = rabbitAdmin.getQueueInfo(RabbitMQConfig.QUEUE_DLQ);
            
            Map<String, Object> healthStatus = new HashMap<>();
            
            if (dlqInfo == null) {
                healthStatus.put("status", "DOWN");
                healthStatus.put("error", "Cannot get DLQ information");
                healthStatus.put("queue", RabbitMQConfig.QUEUE_DLQ);
                return healthStatus;
            }

            int messageCount = dlqInfo.getMessageCount();
            
            healthStatus.put("queue", RabbitMQConfig.QUEUE_DLQ);
            healthStatus.put("messageCount", messageCount);
            healthStatus.put("consumerCount", dlqInfo.getConsumerCount());

            // 임계치에 따른 상태 결정
            if (messageCount >= ERROR_THRESHOLD) {
                healthStatus.put("status", "ERROR");
                healthStatus.put("reason", "Too many messages in DLQ");
                healthStatus.put("threshold", ERROR_THRESHOLD);
            } else if (messageCount >= WARNING_THRESHOLD) {
                healthStatus.put("status", "WARNING");
                healthStatus.put("reason", "High number of messages in DLQ");
                healthStatus.put("threshold", WARNING_THRESHOLD);
            } else {
                healthStatus.put("status", "HEALTHY");
            }

            return healthStatus;

        } catch (Exception e) {
            log.error("Error checking DLQ health", e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("status", "DOWN");
            errorStatus.put("error", e.getMessage());
            errorStatus.put("queue", RabbitMQConfig.QUEUE_DLQ);
            return errorStatus;
        }
    }
}
