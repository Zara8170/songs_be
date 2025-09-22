package com.example.song_be;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SongBeApplicationTests {

    @Test
    @DisplayName("Spring Boot 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인")
    void contextLoads() {
        // Spring Data JPA, JDBC, Redis 설정 충돌 문제가 해결되었다면
        // 이 테스트는 성공적으로 통과할 것입니다.
    }

}
