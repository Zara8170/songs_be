package com.example.song_be.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 서버 상태 확인 API 컨트롤러
 * 서버가 정상적으로 작동하고 있는지 확인하기 위한 헬스체크 기능을 제공합니다.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final String uniqueId = UUID.randomUUID().toString();

    /**
     * 서버 상태 확인
     * 
     * @return 서버 고유 ID를 포함한 상태 메시지
     */
    @GetMapping
    public String checkHealth() {return "Server ID" + uniqueId;}
}
