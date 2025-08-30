package com.example.song_be.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@org.springframework.amqp.rabbit.annotation.EnableRabbit
public class RabbitMQConfig {

    // Recommendation System Constants
    public static final String REC_EXCHANGE = "rec.exchange";
    public static final String DLX_EXCHANGE = "rec.dlx";

    public static final String ROUTING_RECOMMENDATION = "recommendation.generate";
    public static final String ROUTING_RETRY_5S = "recommendation.generate.retry.5s";
    public static final String ROUTING_RETRY_30S = "recommendation.generate.retry.30s";
    public static final String ROUTING_RETRY_120S = "recommendation.generate.retry.120s";
    public static final String ROUTING_DLQ = "recommendation.generate.dlq";

    public static final String QUEUE_MAIN = "rec.recommendation.q";
    public static final String QUEUE_RETRY_5S = "rec.recommendation.retry.5s.q";
    public static final String QUEUE_RETRY_30S = "rec.recommendation.retry.30s.q";
    public static final String QUEUE_RETRY_120S = "rec.recommendation.retry.120s.q";
    public static final String QUEUE_DLQ = "rec.recommendation.dlq";

    // Suggestion System Constants
    public static final String SUGGESTION_EXCHANGE = "suggestion.exchange";
    public static final String SUGGESTION_DLX = "suggestion.dlx";

    public static final String ROUTING_SUGGESTION = "suggestion.send";
    public static final String ROUTING_SUGGESTION_RETRY = "suggestion.send.retry";
    public static final String ROUTING_SUGGESTION_DLQ = "suggestion.send.dlq";

    public static final String QUEUE_SUGGESTION = "suggestion.email.q";
    public static final String QUEUE_SUGGESTION_RETRY = "suggestion.email.retry.q";
    public static final String QUEUE_SUGGESTION_DLQ = "suggestion.email.dlq";

    @Value("${app.rabbitmq.prefetch:16}")
    private int prefetchCount;

    // Exchanges
    @Bean
    public DirectExchange recExchange() {
        return new DirectExchange(REC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange recDlx() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // Main queue
    @Bean
    public Queue recommendationQueue() {
        return QueueBuilder.durable(QUEUE_MAIN)
                .build();
    }

    @Bean
    public Binding bindRecommendationQueue() {
        return BindingBuilder.bind(recommendationQueue())
                .to(recExchange())
                .with(ROUTING_RECOMMENDATION);
    }

    // Retry queues (TTL + DLX back to main exchange)
    @Bean
    public Queue retry5sQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 5000);
        args.put("x-dead-letter-exchange", REC_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_RECOMMENDATION);
        return new Queue(QUEUE_RETRY_5S, true, false, false, args);
    }

    @Bean
    public Binding bindRetry5sQueue() {
        return BindingBuilder.bind(retry5sQueue())
                .to(recDlx())
                .with(ROUTING_RETRY_5S);
    }

    @Bean
    public Queue retry30sQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 30000);
        args.put("x-dead-letter-exchange", REC_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_RECOMMENDATION);
        return new Queue(QUEUE_RETRY_30S, true, false, false, args);
    }

    @Bean
    public Binding bindRetry30sQueue() {
        return BindingBuilder.bind(retry30sQueue())
                .to(recDlx())
                .with(ROUTING_RETRY_30S);
    }

    @Bean
    public Queue retry120sQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 120000);
        args.put("x-dead-letter-exchange", REC_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_RECOMMENDATION);
        return new Queue(QUEUE_RETRY_120S, true, false, false, args);
    }

    @Bean
    public Binding bindRetry120sQueue() {
        return BindingBuilder.bind(retry120sQueue())
                .to(recDlx())
                .with(ROUTING_RETRY_120S);
    }

    // DLQ
    @Bean
    public Queue recommendationDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding bindDlq() {
        return BindingBuilder.bind(recommendationDlq())
                .to(recDlx())
                .with(ROUTING_DLQ);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory manualAckContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(prefetchCount);
        factory.setDefaultRequeueRejected(false);
        factory.setMessageConverter(jackson2Converter());
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2Converter());
        return template;
    }

    @Bean
    public org.springframework.amqp.support.converter.Jackson2JsonMessageConverter jackson2Converter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }

    // ============ Suggestion System Configuration ============

    // Suggestion Exchanges
    @Bean
    public DirectExchange suggestionExchange() {
        return new DirectExchange(SUGGESTION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange suggestionDlx() {
        return new DirectExchange(SUGGESTION_DLX, true, false);
    }

    // Suggestion Main Queue
    @Bean
    public Queue suggestionQueue() {
        return QueueBuilder.durable(QUEUE_SUGGESTION)
                .build();
    }

    @Bean
    public Binding bindSuggestionQueue() {
        return BindingBuilder.bind(suggestionQueue())
                .to(suggestionExchange())
                .with(ROUTING_SUGGESTION);
    }

    // Suggestion Retry Queue (30s TTL)
    @Bean
    public Queue suggestionRetryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 30000); // 30초 후 재시도
        args.put("x-dead-letter-exchange", SUGGESTION_EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_SUGGESTION);
        return new Queue(QUEUE_SUGGESTION_RETRY, true, false, false, args);
    }

    @Bean
    public Binding bindSuggestionRetryQueue() {
        return BindingBuilder.bind(suggestionRetryQueue())
                .to(suggestionDlx())
                .with(ROUTING_SUGGESTION_RETRY);
    }

    // Suggestion Dead Letter Queue
    @Bean
    public Queue suggestionDlq() {
        return QueueBuilder.durable(QUEUE_SUGGESTION_DLQ).build();
    }

    @Bean
    public Binding bindSuggestionDlq() {
        return BindingBuilder.bind(suggestionDlq())
                .to(suggestionDlx())
                .with(ROUTING_SUGGESTION_DLQ);
    }
}


