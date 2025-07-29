package com.example.song_be.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))  // 연결 타임아웃 30초
                .setReadTimeout(Duration.ofSeconds(60))     // 읽기 타임아웃 60초
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }
    
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);  // 30초
        factory.setReadTimeout(60000);     // 60초
        factory.setBufferRequestBody(false); // 대용량 응답 처리를 위해 스트리밍 모드 사용
        return factory;
    }
}
