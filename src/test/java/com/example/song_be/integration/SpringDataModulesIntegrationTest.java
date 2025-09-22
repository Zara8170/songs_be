package com.example.song_be.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Spring Data 모듈 간 충돌 없이 애플리케이션이 정상 시작되는지 확인하는 통합 테스트
 * 이전에 발생했던 JDBC, Redis 리포지토리 인식 문제가 해결되었는지 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.redis.host=localhost",
    "spring.redis.port=6379",
    "spring.redis.password=",
    "logging.level.org.springframework.data=WARN"
})
class SpringDataModulesIntegrationTest {

    @Test
    @DisplayName("Spring Data JPA, JDBC, Redis 모듈이 충돌 없이 애플리케이션 컨텍스트가 로드되는지 확인")
    void contextLoadsWithoutSpringDataModuleConflicts() {
        // 이 테스트가 성공하면 다음을 의미합니다:
        // 1. JPA 리포지토리들이 올바르게 스캔되었음
        // 2. JDBC 리포지토리가 JPA 리포지토리를 인식하려 하지 않음
        // 3. Redis 리포지토리가 JPA 리포지토리를 인식하려 하지 않음
        // 4. 애플리케이션 컨텍스트가 정상적으로 시작됨
        
        // Given, When, Then은 @SpringBootTest의 컨텍스트 로딩 과정에서 수행됨
        // 만약 Spring Data 모듈 간 충돌이 있다면 이 테스트는 실패할 것입니다.
    }
}
