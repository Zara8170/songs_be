package com.example.song_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableRedisRepositories(basePackages = {})
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("[RedisConfig] Configuring Redis connection to {}:{}", host, port);
        log.info("[RedisConfig] Redis password configured: {}", password != null && !password.isEmpty() ? "YES" : "NO");
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
            log.info("[RedisConfig] Redis password applied");
        }
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        log.info("[RedisConfig] Redis connection factory created successfully");
        return factory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        log.info("[RedisConfig] Creating RedisTemplate...");
        
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        
        // Key-Value 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        
        log.info("[RedisConfig] RedisTemplate created and configured successfully");
        return template;
    }
} 