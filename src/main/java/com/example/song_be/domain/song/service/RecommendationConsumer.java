package com.example.song_be.domain.song.service;

import com.example.song_be.config.RabbitMQConfig;
import com.example.song_be.domain.like.service.SongLikeService;
import com.example.song_be.domain.playlist.repository.PlaylistSongRepository;
import com.example.song_be.domain.song.dto.RecommendationJobError;
import com.example.song_be.domain.song.dto.RecommendationJobMessage;
import com.example.song_be.domain.song.dto.RecommendationRequestDTO;
import com.example.song_be.domain.song.dto.RecommendationResponseFromPythonDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import com.example.song_be.config.LogbackJsonConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationConsumer {

    private final RecommendationJobStore jobStore;
    private final SongLikeService songLikeService;
    private final PlaylistSongRepository playlistSongRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RestRecommendationCaller restCaller;
    private final RecommendationMetrics metrics;

    private static final int TIMEOUT_SECONDS = 60;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_MAIN, containerFactory = "manualAckContainerFactory")
    public void handle(RecommendationJobMessage message, com.rabbitmq.client.Channel channel, Message amqpMessage,
                       @Header(value = "x-death", required = false) List<Map<String, Object>> xDeath) throws Exception {
        String jobId = message.getJobId();
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();

        try {
            MDC.put(LogbackJsonConfig.MDC_JOB_ID, jobId);
            if (message.getTraceId() != null) MDC.put(LogbackJsonConfig.MDC_TRACE_ID, message.getTraceId());
            if (message.getMemberId() != null) MDC.put(LogbackJsonConfig.MDC_MEMBER_ID, String.valueOf(message.getMemberId()));
            log.info("[rec] Start jobId={} memberId={} attempt={}", jobId, message.getMemberId(), message.getAttempt());
            jobStore.setRunning(jobId);

            List<Long> preferred = resolvePreferredSongs(message);

            RecommendationRequestDTO req = RecommendationRequestDTO.builder()
                    .memberId(String.valueOf(message.getMemberId()))
                    .favoriteSongIds(preferred)
                    .build();

            Instant start = Instant.now();
            RecommendationResponseFromPythonDTO result = restCaller.callWithTimeout(req, Duration.ofSeconds(TIMEOUT_SECONDS));
            jobStore.setSucceeded(jobId, result);
            jobStore.setLatestForMember(message.getMemberId(), result);
            channel.basicAck(deliveryTag, false);
            metrics.incrementSucceeded(message.getSource());
            metrics.observeLatency(Duration.between(start, Instant.now()));
            log.info("[rec] Succeeded jobId={} in {} ms", jobId, Duration.between(start, Instant.now()).toMillis());
        } catch (TransientException e) {
            int attempt = Optional.ofNullable(message.getAttempt()).orElse(0) + 1;
            log.warn("[rec] Transient failure jobId={} attempt={} cause={}", jobId, attempt, e.getMessage());
            requeueWithBackoff(message, attempt);
            channel.basicAck(deliveryTag, false); // ack original; retried via new message
            metrics.incrementRetried(attempt);
        } catch (Exception e) {
            log.error("[rec] Permanent failure jobId={} cause=", jobId, e);
            jobStore.setFailed(jobId, RecommendationJobError.builder()
                    .code("MODEL_ERROR")
                    .message(e.getMessage())
                    .build());
            channel.basicAck(deliveryTag, false);
            // Send to DLQ
            rabbitTemplate.convertAndSend(RabbitMQConfig.DLX_EXCHANGE, RabbitMQConfig.ROUTING_DLQ, message);
            metrics.incrementFailed("permanent");
        } finally {
            MDC.remove(LogbackJsonConfig.MDC_JOB_ID);
            MDC.remove(LogbackJsonConfig.MDC_TRACE_ID);
            MDC.remove(LogbackJsonConfig.MDC_MEMBER_ID);
        }
    }

    private List<Long> resolvePreferredSongs(RecommendationJobMessage msg) {
        if (msg.getFavoriteSongIds() != null && !msg.getFavoriteSongIds().isEmpty()) {
            return msg.getFavoriteSongIds();
        }
        List<Long> liked = songLikeService.getLikedSongIds(msg.getMemberId());
        List<Long> playlist = playlistSongRepository.findSongIdsByMemberId(msg.getMemberId());
        Set<Long> merged = new HashSet<>(liked);
        merged.addAll(playlist);
        return new ArrayList<>(merged);
    }

    private void requeueWithBackoff(RecommendationJobMessage msg, int attempt) {
        if (attempt == 1) {
            msg.setAttempt(attempt);
            rabbitTemplate.convertAndSend(RabbitMQConfig.DLX_EXCHANGE, RabbitMQConfig.ROUTING_RETRY_5S, msg);
        } else if (attempt == 2) {
            msg.setAttempt(attempt);
            rabbitTemplate.convertAndSend(RabbitMQConfig.DLX_EXCHANGE, RabbitMQConfig.ROUTING_RETRY_30S, msg);
        } else if (attempt == 3) {
            msg.setAttempt(attempt);
            rabbitTemplate.convertAndSend(RabbitMQConfig.DLX_EXCHANGE, RabbitMQConfig.ROUTING_RETRY_120S, msg);
        } else {
            // exhausted
            jobStore.setFailed(msg.getJobId(), RecommendationJobError.builder()
                    .code("GENERATION_TIMEOUT")
                    .message("Retry attempts exhausted")
                    .build());
            rabbitTemplate.convertAndSend(RabbitMQConfig.DLX_EXCHANGE, RabbitMQConfig.ROUTING_DLQ, msg);
        }
    }

    // Inner collaborator that performs HTTP call with timeout
    @Component
    @RequiredArgsConstructor
    public static class RestRecommendationCaller {
        private final org.springframework.web.client.RestTemplate restTemplate;
        private final ObjectMapper objectMapper;
        @org.springframework.beans.factory.annotation.Value("${app.props.python-server-url}")
        private String pythonServerUrl;

        public RecommendationResponseFromPythonDTO callWithTimeout(RecommendationRequestDTO req, Duration timeout) {
            var start = Instant.now();
            try {
                var response = restTemplate.postForEntity(pythonServerUrl + "/recommend", req, String.class);
                String raw = response.getBody();
                if (raw == null) throw new RuntimeException("Empty response from model server");
                return objectMapper.readValue(raw, RecommendationResponseFromPythonDTO.class);
            } catch (Exception e) {
                if (Duration.between(start, Instant.now()).compareTo(timeout) >= 0) {
                    throw new TransientException("Model call timeout");
                }
                // Classify transient vs permanent simply; this can be improved
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                    throw new TransientException(e.getMessage());
                }
                throw new RuntimeException(e);
            }
        }
    }

    static class TransientException extends RuntimeException {
        public TransientException(String message) { super(message); }
    }
}


