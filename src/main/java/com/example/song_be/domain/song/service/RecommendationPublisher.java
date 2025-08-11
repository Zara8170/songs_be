package com.example.song_be.domain.song.service;

import com.example.song_be.config.RabbitMQConfig;
import com.example.song_be.config.LogbackJsonConfig;
import com.example.song_be.domain.song.dto.RecommendationJobMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;

@Service
@RequiredArgsConstructor
public class RecommendationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RecommendationMetrics metrics;

    public void publish(RecommendationJobMessage message) {
        if (message.getJobId() != null) {
            MDC.put(LogbackJsonConfig.MDC_JOB_ID, message.getJobId());
        }
        if (message.getTraceId() != null) {
            MDC.put(LogbackJsonConfig.MDC_TRACE_ID, message.getTraceId());
        }
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.REC_EXCHANGE,
                RabbitMQConfig.ROUTING_RECOMMENDATION,
                message
        );
        metrics.incrementEnqueued(message.getSource());
        MDC.remove(LogbackJsonConfig.MDC_JOB_ID);
        MDC.remove(LogbackJsonConfig.MDC_TRACE_ID);
    }
}


