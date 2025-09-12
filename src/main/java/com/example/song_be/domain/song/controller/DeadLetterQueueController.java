package com.example.song_be.domain.song.controller;

import com.example.song_be.config.RabbitMQConfig;
import com.example.song_be.domain.song.service.DeadLetterQueueHealthIndicator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Dead Letter Queue 관리를 위한 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dlq")
@RequiredArgsConstructor
@Tag(name = "Dead Letter Queue Management", description = "Dead Letter Queue 관리 API")
public class DeadLetterQueueController {

    private final RabbitAdmin rabbitAdmin;
    private final DeadLetterQueueHealthIndicator healthIndicator;

    @Operation(summary = "DLQ 상태 조회", description = "Dead Letter Queue의 현재 상태를 조회합니다")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDlqStatus() {
        try {
            QueueInformation dlqInfo = rabbitAdmin.getQueueInfo(RabbitMQConfig.QUEUE_DLQ);
            
            Map<String, Object> response = new HashMap<>();
            if (dlqInfo != null) {
                response.put("queueName", RabbitMQConfig.QUEUE_DLQ);
                response.put("messageCount", dlqInfo.getMessageCount());
                response.put("consumerCount", dlqInfo.getConsumerCount());
                response.put("status", "AVAILABLE");
            } else {
                response.put("queueName", RabbitMQConfig.QUEUE_DLQ);
                response.put("status", "NOT_AVAILABLE");
                response.put("error", "Queue information not available");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting DLQ status", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "DLQ 메시지 수 조회", description = "Dead Letter Queue에 있는 메시지 수를 조회합니다")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getDlqMessageCount() {
        try {
            QueueInformation dlqInfo = rabbitAdmin.getQueueInfo(RabbitMQConfig.QUEUE_DLQ);
            
            Map<String, Object> response = new HashMap<>();
            if (dlqInfo != null) {
                response.put("messageCount", dlqInfo.getMessageCount());
                response.put("timestamp", System.currentTimeMillis());
            } else {
                response.put("messageCount", -1);
                response.put("error", "Queue not available");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting DLQ message count", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("messageCount", -1);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "DLQ 메시지 퍼지", description = "Dead Letter Queue의 모든 메시지를 삭제합니다 (주의: 복구 불가능)")
    @DeleteMapping("/purge")
    public ResponseEntity<Map<String, Object>> purgeDlq() {
        try {
            rabbitAdmin.purgeQueue(RabbitMQConfig.QUEUE_DLQ);
            
            Map<String, Object> response = new HashMap<>();
            response.put("result", "SUCCESS");
            response.put("message", "DLQ messages purged successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            log.warn("DLQ messages purged by admin");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error purging DLQ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("result", "ERROR");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "모든 큐 정보 조회", description = "추천 시스템의 모든 큐 상태를 조회합니다")
    @GetMapping("/queues/all")
    public ResponseEntity<Map<String, Object>> getAllQueuesStatus() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // 모든 큐 정보 조회
            String[] queueNames = {
                RabbitMQConfig.QUEUE_MAIN,
                RabbitMQConfig.QUEUE_RETRY_5S,
                RabbitMQConfig.QUEUE_RETRY_30S,
                RabbitMQConfig.QUEUE_RETRY_120S,
                RabbitMQConfig.QUEUE_DLQ
            };
            
            for (String queueName : queueNames) {
                QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);
                Map<String, Object> queueData = new HashMap<>();
                
                if (queueInfo != null) {
                    queueData.put("messageCount", queueInfo.getMessageCount());
                    queueData.put("consumerCount", queueInfo.getConsumerCount());
                    queueData.put("status", "AVAILABLE");
                } else {
                    queueData.put("status", "NOT_AVAILABLE");
                }
                
                response.put(queueName, queueData);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all queues status", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "DLQ 헬스 체크", description = "Dead Letter Queue의 헬스 상태를 체크합니다")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getDlqHealth() {
        Map<String, Object> healthStatus = healthIndicator.getHealthStatus();
        return ResponseEntity.ok(healthStatus);
    }
}
