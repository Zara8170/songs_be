# Song Backend API

Spring Boot 기반의 음악 관련 백엔드 API 서비스입니다.

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#-시스템-아키텍처)
- [시작하기](#-시작하기)
- [환경 설정](#-환경-설정)
- [API 문서](#-api-문서)
- [Docker 실행](#-docker-실행)
- [개발 환경 설정](#-개발-환경-설정)
- [모니터링](#-모니터링)

## 🎯 프로젝트 개요

Song Backend는 음악 스트리밍 서비스를 위한 RESTful API를 제공합니다. 사용자 인증, 음악 관리, 플레이리스트 관리, 좋아요 기능 등을 포함한 종합적인 음악 서비스 백엔드입니다.

### 주요 기능

- 🔐 **사용자 인증 및 권한 관리** (JWT, OAuth2 Google 로그인)
- 🎵 **음악 관리** (업로드, 검색, 메타데이터 관리)
- 📝 **플레이리스트 관리** (생성, 수정, 공유)
- ❤️ **좋아요 시스템**
- 🎌 **애니메이션 관련 음악 카테고리**
- 🔍 **Elasticsearch 기반 검색**
- 📊 **실시간 메시징** (RabbitMQ)

## 🛠 기술 스택

### Backend Framework

- **Spring Boot 3.3.0**
- **Java 17**
- **Spring Security**
- **Spring Data JPA**
- **QueryDSL**

### Database & Storage

- **MySQL** (Primary Database)
- **Redis** (Caching & Session)
- **Elasticsearch** (Search Engine)

### Message Queue & Communication

- **RabbitMQ** (Message Broker)
- **REST API**

### Security & Authentication

- **JWT** (JSON Web Token)
- **OAuth2** (Google Social Login)
- **AES Encryption**

### Monitoring & Logging

- **Spring Boot Actuator**
- **Prometheus** (Metrics)
- **Logback** (Structured JSON Logging)

### DevOps & Deployment

- **Docker & Docker Compose**
- **Gradle**
- **Flyway** (Database Migration)

## 🏗 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │───▶│  Spring Boot    │───▶│     MySQL       │
│                 │    │   (Port 8082)   │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ├───▶ ┌─────────────────┐
                                │     │     Redis       │
                                │     │   (Cache)       │
                                │     └─────────────────┘
                                │
                                ├───▶ ┌─────────────────┐
                                │     │ Elasticsearch   │
                                │     │   (Search)      │
                                │     └─────────────────┘
                                │
                                └───▶ ┌─────────────────┐
                                      │   RabbitMQ      │
                                      │ (Port 5672)     │
                                      └─────────────────┘
```

## 🚀 시작하기

### 필수 요구사항

- **Java 17** 이상
- **Docker & Docker Compose**
- **MySQL 8.0** 이상
- **Redis**
- **Elasticsearch 8.x**

### 빠른 실행 (Docker Compose)

1. **저장소 클론**

   ```bash
   git clone <repository-url>
   cd song_be
   ```

2. **환경 변수 설정**

   ```bash
   cp .env.example .env
   # .env 파일을 편집하여 필요한 환경 변수 설정
   ```

3. **Docker Compose로 실행**

   ```bash
   docker-compose up -d
   ```

4. **서비스 확인**
   - API 서버: http://localhost:8082
   - RabbitMQ 관리 UI: http://localhost:15672
   - API 문서: http://localhost:8082/swagger-ui.html

### Profile별 설정

- **local**: 개발 환경용 설정
- **prod**: 운영 환경용 설정

## 📚 API 문서

### Swagger UI

서버 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8082/v3/api-docs

### 주요 API 엔드포인트

```
GET    /api/health              # 헬스 체크
POST   /api/auth/login          # 로그인
POST   /api/auth/register       # 회원가입
GET    /api/songs               # 음악 목록 조회
POST   /api/songs               # 음악 업로드
GET    /api/playlists           # 플레이리스트 목록
POST   /api/playlists           # 플레이리스트 생성
POST   /api/likes               # 좋아요 추가
DELETE /api/likes/{id}          # 좋아요 취소
```

## 🐳 Docker 실행

### 개발 환경

```bash
# 애플리케이션 빌드
./gradlew bootJar

# Docker 이미지 빌드
docker build -t song-be:latest .

# Docker Compose로 전체 스택 실행
docker-compose up -d
```

### 운영 환경

```bash
# 환경 변수 설정
export DOCKER_IMAGE_NAME=your-registry/song-be
export SPRING_PROFILES_ACTIVE=prod

# Docker Compose 실행
docker-compose -f docker-compose.yml up -d
```

## 💻 개발 환경 설정

### 로컬 개발 실행

1. **데이터베이스 준비**

   ```bash
   # MySQL, Redis, Elasticsearch, RabbitMQ가 로컬에 설치되어 있어야 합니다
   ```

2. **환경 변수 설정**

   ```bash
   export SPRING_PROFILES_ACTIVE=local
   # 기타 필요한 환경 변수들...
   ```

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.example.song_be.SongBeApplicationTests"
```

### QueryDSL Q클래스 생성

```bash
./gradlew compileJava
```

## 📊 모니터링

### Health Check

- **엔드포인트**: `GET /actuator/health`
- **Prometheus 메트릭**: `GET /actuator/prometheus`

### 로그 확인

```bash
# Docker 컨테이너 로그
docker-compose logs -f app

# 특정 시간대 로그
docker-compose logs --since="2024-01-01T00:00:00" app
```

### RabbitMQ 관리

- **관리 UI**: http://localhost:15672
- **기본 계정**: guest/guest

## 🔧 트러블슈팅

### 일반적인 문제들

1. **포트 충돌**

   ```bash
   # 포트 사용 확인
   netstat -tulpn | grep :8082
   ```

2. **데이터베이스 연결 실패**

   - MySQL 서비스 상태 확인
   - 환경 변수 설정 확인
   - 방화벽 설정 확인

3. **Docker 메모리 부족**
   ```bash
   # Docker 리소스 정리
   docker system prune -a
   ```

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 👥 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 연락처

프로젝트에 대한 질문이나 제안사항이 있으시면 이슈를 생성해 주세요.
