package com.example.song_be.domain.song.service;

import com.example.song_be.config.LogbackJsonConfig;
import com.example.song_be.config.RabbitMQConfig;
import com.example.song_be.domain.song.dto.RecommendationJobMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Dead Letter Queue 메시지를 처리하는 전용 컨슈머
 * DLQ에 도착한 메시지들을 로깅하고 모니터링 메트릭을 업데이트합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueConsumer {

    private final RecommendationJobStore jobStore;
    private final RecommendationMetrics metrics;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DLQ, containerFactory = "dlqContainerFactory")
    public void handleDeadLetter(RecommendationJobMessage message, 
                                com.rabbitmq.client.Channel channel, 
                                Message amqpMessage,
                                @Header(value = "x-death", required = false) List<Map<String, Object>> xDeath) throws Exception {
        
        String jobId = message.getJobId();
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        try {
            MDC.put(LogbackJsonConfig.MDC_JOB_ID, jobId);
            if (message.getTraceId() != null) MDC.put(LogbackJsonConfig.MDC_TRACE_ID, message.getTraceId());
            if (message.getMemberId() != null) MDC.put(LogbackJsonConfig.MDC_MEMBER_ID, String.valueOf(message.getMemberId()));

            // Death 정보 로깅
            if (xDeath != null && !xDeath.isEmpty()) {
                Map<String, Object> deathInfo = xDeath.get(0);
                Object count = deathInfo.get("count");
                Object reason = deathInfo.get("reason");
                Object queue = deathInfo.get("queue");
                Object exchange = deathInfo.get("exchange");
                Object routingKeys = deathInfo.get("routing-keys");
                
                log.error("[dlq] Message arrived in DLQ - jobId={} memberId={} count={} reason={} originalQueue={} originalExchange={} routingKeys={}", 
                    jobId, message.getMemberId(), count, reason, queue, exchange, routingKeys);
            } else {
                log.error("[dlq] Message arrived in DLQ - jobId={} memberId={} (no death info)", 
                    jobId, message.getMemberId());
            }

            // 메트릭 업데이트
            metrics.incrementDeadLetter();

            // Job Store에 최종 실패 상태 기록 (아직 실패로 마킹되지 않은 경우)
            if (!jobStore.isJobFinished(jobId)) {
                jobStore.setFailed(jobId, com.example.song_be.domain.song.dto.RecommendationJobError.builder()
                    .code("DLQ_ARRIVED")
                    .message("Message reached Dead Letter Queue after exhausting all retry attempts")
                    .build());
            }

            // DLQ 메시지는 항상 ACK (재처리하지 않음)
            channel.basicAck(deliveryTag, false);
            
            log.info("[dlq] Dead letter message processed and acknowledged - jobId={}", jobId);

        } catch (Exception e) {
            log.error("[dlq] Error processing dead letter message - jobId={}", jobId, e);
            // DLQ 처리 중 오류가 발생해도 ACK (무한 루프 방지)
            channel.basicAck(deliveryTag, false);
        } finally {
            MDC.remove(LogbackJsonConfig.MDC_JOB_ID);
            MDC.remove(LogbackJsonConfig.MDC_TRACE_ID);
            MDC.remove(LogbackJsonConfig.MDC_MEMBER_ID);
        }
    }
}
