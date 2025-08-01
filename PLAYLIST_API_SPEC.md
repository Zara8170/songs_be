# 플레이리스트 API 명세서

## 개요

사용자의 개인 플레이리스트를 관리할 수 있는 API입니다. 플레이리스트 생성, 수정, 삭제 및 곡 관리 기능을 제공합니다.

## 인증

모든 API는 JWT 토큰을 통한 인증이 필요합니다.

```
Authorization: Bearer {JWT_TOKEN}
```

## 공통 응답 형식

### 성공 응답

```json
{
  "data": {
    /* 응답 데이터 */
  },
  "message": "성공 메시지"
}
```

### 에러 응답

```json
{
  "error": "에러 메시지",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-01T00:00:00"
}
```

---

## 1. 플레이리스트 CRUD

### 1.1 플레이리스트 생성

```
POST /api/playlist
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**요청 본문 (PlaylistCreateDTO)**

```json
{
  "title": "내가 좋아하는 K-Pop",
  "description": "최근에 듣고 있는 K-Pop 모음",
  "isPublic": true
}
```

**필드 설명**

- `title` (required): 플레이리스트 제목 (최대 100자)
- `description` (optional): 플레이리스트 설명 (최대 500자)
- `isPublic` (optional): 공개 여부 (기본값: true)

**성공 응답 (200)**

```json
{
  "playlistId": 1,
  "title": "내가 좋아하는 K-Pop",
  "description": "최근에 듣고 있는 K-Pop 모음",
  "isPublic": true,
  "memberId": 123,
  "memberEmail": "user@example.com",
  "createdAt": "2024-01-01T10:00:00",
  "modifiedAt": "2024-01-01T10:00:00",
  "songCount": 0
}
```

### 1.2 플레이리스트 상세 조회

```
GET /api/playlist/{playlistId}
Authorization: Bearer {JWT_TOKEN}
```

**경로 변수**

- `playlistId`: 플레이리스트 ID

**성공 응답 (200)**

```json
{
  "playlistId": 1,
  "title": "내가 좋아하는 K-Pop",
  "description": "최근에 듣고 있는 K-Pop 모음",
  "isPublic": true,
  "memberId": 123,
  "memberEmail": "user@example.com",
  "createdAt": "2024-01-01T10:00:00",
  "modifiedAt": "2024-01-01T10:00:00",
  "songCount": 2,
  "songs": [
    {
      "id": 1,
      "playlistId": 1,
      "order": 1,
      "song": {
        "songId": 101,
        "title_kr": "Dynamite",
        "artist": "BTS",
        "artist_kr": "방탄소년단",
        "tj_number": 12345,
        "ky_number": 67890,
        "lang": "EN",
        "genre": "Pop",
        "mood": "Energetic"
      }
    }
  ]
}
```

### 1.3 플레이리스트 수정

```
PUT /api/playlist/{playlistId}
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**요청 본문 (PlaylistUpdateDTO)**

```json
{
  "title": "수정된 플레이리스트 제목",
  "description": "수정된 설명",
  "isPublic": false
}
```

**필드 설명**

- 모든 필드는 선택사항 (null인 필드는 업데이트하지 않음)
- `title`: 플레이리스트 제목 (최대 100자)
- `description`: 플레이리스트 설명 (최대 500자)
- `isPublic`: 공개 여부

**성공 응답 (200)**

```json
{
  "playlistId": 1,
  "title": "수정된 플레이리스트 제목",
  "description": "수정된 설명",
  "isPublic": false,
  "memberId": 123,
  "memberEmail": "user@example.com",
  "createdAt": "2024-01-01T10:00:00",
  "modifiedAt": "2024-01-01T11:00:00",
  "songCount": 2
}
```

### 1.4 플레이리스트 삭제

```
DELETE /api/playlist/{playlistId}
Authorization: Bearer {JWT_TOKEN}
```

**경로 변수**

- `playlistId`: 플레이리스트 ID

**성공 응답 (204 No Content)**

---

## 2. 플레이리스트 목록 조회

### 2.1 내 플레이리스트 목록 조회

```
GET /api/playlist/my
Authorization: Bearer {JWT_TOKEN}
```

**성공 응답 (200)**

```json
[
  {
    "playlistId": 1,
    "title": "내가 좋아하는 K-Pop",
    "description": "최근에 듣고 있는 K-Pop 모음",
    "isPublic": true,
    "memberId": 123,
    "memberEmail": "user@example.com",
    "createdAt": "2024-01-01T10:00:00",
    "modifiedAt": "2024-01-01T10:00:00",
    "songCount": 5
  }
]
```

### 2.2 내 플레이리스트 목록 조회 (페이징)

```
GET /api/playlist/my/paging?page=1&size=10&sort=desc
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 파라미터**

- `page` (optional): 페이지 번호 (기본값: 1)
- `size` (optional): 페이지 크기 (기본값: 10)
- `sort` (optional): 정렬 순서 ("asc" 또는 "desc", 기본값: "desc")

**성공 응답 (200)**

```json
{
  "dtoList": [
    {
      "playlistId": 1,
      "title": "내가 좋아하는 K-Pop",
      "description": "최근에 듣고 있는 K-Pop 모음",
      "isPublic": true,
      "memberId": 123,
      "memberEmail": "user@example.com",
      "createdAt": "2024-01-01T10:00:00",
      "modifiedAt": "2024-01-01T10:00:00",
      "songCount": 5
    }
  ],
  "pageNumList": [1, 2, 3],
  "pageRequestDTO": {
    "page": 1,
    "size": 10,
    "sort": "desc"
  },
  "prev": false,
  "next": true,
  "totalCount": 25,
  "prevPage": 0,
  "nextPage": 2,
  "totalPage": 3,
  "current": 1
}
```

### 2.3 공개 플레이리스트 목록 조회

```
GET /api/playlist/public?page=1&size=10&sort=desc
```

**쿼리 파라미터**

- `page` (optional): 페이지 번호 (기본값: 1)
- `size` (optional): 페이지 크기 (기본값: 10)
- `sort` (optional): 정렬 순서 ("asc" 또는 "desc", 기본값: "desc")

**성공 응답 (200)**

- 2.2와 동일한 페이징 형식

### 2.4 플레이리스트 검색

```
GET /api/playlist/search?title=K-Pop
Authorization: Bearer {JWT_TOKEN}
```

**쿼리 파라미터**

- `title` (required): 검색할 제목

**성공 응답 (200)**

```json
[
  {
    "playlistId": 1,
    "title": "내가 좋아하는 K-Pop",
    "description": "최근에 듣고 있는 K-Pop 모음",
    "isPublic": true,
    "memberId": 123,
    "memberEmail": "user@example.com",
    "createdAt": "2024-01-01T10:00:00",
    "modifiedAt": "2024-01-01T10:00:00",
    "songCount": 5
  }
]
```

---

## 3. 플레이리스트 곡 관리

### 3.1 플레이리스트에 곡 추가

```
POST /api/playlist/{playlistId}/songs
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**요청 본문 (PlaylistAddSongDTO)**

```json
{
  "songId": 101,
  "order": 3
}
```

**필드 설명**

- `songId` (required): 추가할 곡의 ID
- `order` (optional): 곡의 순서 (지정하지 않으면 마지막에 추가)

**성공 응답 (200)**

```json
{
  "id": 1,
  "playlistId": 1,
  "order": 3,
  "song": {
    "songId": 101,
    "title_kr": "Dynamite",
    "artist": "BTS",
    "artist_kr": "방탄소년단",
    "tj_number": 12345,
    "ky_number": 67890,
    "lang": "EN",
    "genre": "Pop",
    "mood": "Energetic"
  }
}
```

### 3.2 플레이리스트에서 곡 삭제

```
DELETE /api/playlist/{playlistId}/songs/{songId}
Authorization: Bearer {JWT_TOKEN}
```

**경로 변수**

- `playlistId`: 플레이리스트 ID
- `songId`: 삭제할 곡의 ID

**성공 응답 (204 No Content)**

### 3.3 플레이리스트 곡 목록 조회

```
GET /api/playlist/{playlistId}/songs
Authorization: Bearer {JWT_TOKEN}
```

**경로 변수**

- `playlistId`: 플레이리스트 ID

**성공 응답 (200)**

```json
[
  {
    "id": 1,
    "playlistId": 1,
    "order": 1,
    "song": {
      "songId": 101,
      "title_kr": "Dynamite",
      "artist": "BTS",
      "artist_kr": "방탄소년단",
      "tj_number": 12345,
      "ky_number": 67890,
      "lang": "EN",
      "genre": "Pop",
      "mood": "Energetic",
      "likeCount": 1250,
      "likedByMe": true
    }
  },
  {
    "id": 2,
    "playlistId": 1,
    "order": 2,
    "song": {
      "songId": 102,
      "title_kr": "봄날",
      "artist": "BTS",
      "artist_kr": "방탄소년단",
      "tj_number": 12346,
      "ky_number": 67891,
      "lang": "KO",
      "genre": "Ballad",
      "mood": "Emotional",
      "likeCount": 980,
      "likedByMe": false
    }
  }
]
```

### 3.4 플레이리스트 곡 순서 변경

```
PUT /api/playlist/{playlistId}/songs/{songId}/order?newOrder=1
Authorization: Bearer {JWT_TOKEN}
```

**경로 변수**

- `playlistId`: 플레이리스트 ID
- `songId`: 순서를 변경할 곡의 ID

**쿼리 파라미터**

- `newOrder` (required): 새로운 순서 (1부터 시작)

**성공 응답 (204 No Content)**

---

## 4. 에러 코드

| HTTP Status | 에러 메시지                                       | 설명                                  |
| ----------- | ------------------------------------------------- | ------------------------------------- |
| 400         | "플레이리스트 제목은 필수입니다."                 | 제목이 비어있음                       |
| 400         | "플레이리스트 제목은 100자를 초과할 수 없습니다." | 제목이 너무 긺                        |
| 400         | "설명은 500자를 초과할 수 없습니다."              | 설명이 너무 긺                        |
| 400         | "곡 ID는 필수입니다."                             | songId가 비어있음                     |
| 400         | "이미 플레이리스트에 추가된 곡입니다."            | 중복 곡 추가 시도                     |
| 400         | "유효하지 않은 순서입니다."                       | 잘못된 order 값                       |
| 401         | "인증이 필요합니다."                              | JWT 토큰 누락 또는 무효               |
| 403         | "접근 권한이 없습니다."                           | 타인의 비공개 플레이리스트 접근       |
| 403         | "수정 권한이 없습니다."                           | 타인의 플레이리스트 수정 시도         |
| 403         | "삭제 권한이 없습니다."                           | 타인의 플레이리스트 삭제 시도         |
| 403         | "곡 추가 권한이 없습니다."                        | 타인의 플레이리스트에 곡 추가 시도    |
| 403         | "곡 삭제 권한이 없습니다."                        | 타인의 플레이리스트에서 곡 삭제 시도  |
| 403         | "순서 변경 권한이 없습니다."                      | 타인의 플레이리스트 곡 순서 변경 시도 |
| 404         | "존재하지 않는 플레이리스트입니다."               | 잘못된 플레이리스트 ID                |
| 404         | "존재하지 않는 곡입니다."                         | 잘못된 곡 ID                          |
| 404         | "존재하지 않는 회원입니다."                       | 잘못된 회원 ID                        |
| 404         | "플레이리스트에 해당 곡이 없습니다."              | 플레이리스트에 없는 곡 삭제/수정 시도 |

---

## 5. 데이터 타입 정의

### Song 객체

```json
{
  "songId": 101,
  "tj_number": 12345,
  "ky_number": 67890,
  "title_kr": "한국어 제목",
  "title_en": "English Title",
  "title_en_kr": "영어 제목 한국어 표기",
  "title_jp": "日本語タイトル",
  "title_yomi": "にほんごたいとる",
  "title_yomi_kr": "니혼고 타이토루",
  "lang": "KO|EN|JP",
  "genre": "Pop|Ballad|Rock|...",
  "mood": "Happy|Sad|Energetic|...",
  "artist": "Artist Name",
  "artist_kr": "아티스트 한국어 이름",
  "lyrics_original": "원본 가사",
  "lyrics_yomi": "요미가나 가사",
  "lyrics_kr": "한국어 가사",
  "likeCount": 1250,
  "likedByMe": true
}
```

### 페이징 응답 형식

```json
{
  "dtoList": [
    /* 데이터 배열 */
  ],
  "pageNumList": [1, 2, 3, 4, 5],
  "pageRequestDTO": {
    "page": 1,
    "size": 10,
    "sort": "desc"
  },
  "prev": false,
  "next": true,
  "totalCount": 50,
  "prevPage": 0,
  "nextPage": 2,
  "totalPage": 5,
  "current": 1
}
```

---

## 6. 사용 예시

### 플레이리스트 생성 후 곡 추가하기

```javascript
// 1. 플레이리스트 생성
const createResponse = await fetch("/api/playlist", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + token,
  },
  body: JSON.stringify({
    title: "새 플레이리스트",
    description: "테스트 플레이리스트",
    isPublic: true,
  }),
});
const playlist = await createResponse.json();

// 2. 곡 추가
const addSongResponse = await fetch(`/api/playlist/${playlist.playlistId}/songs`, {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: "Bearer " + token,
  },
  body: JSON.stringify({
    songId: 101,
  }),
});
```

### 페이징으로 플레이리스트 목록 조회

```javascript
const response = await fetch("/api/playlist/my/paging?page=1&size=5&sort=desc", {
  headers: {
    Authorization: "Bearer " + token,
  },
});
const paginatedPlaylists = await response.json();

console.log(`총 ${paginatedPlaylists.totalCount}개의 플레이리스트 중 ${paginatedPlaylists.dtoList.length}개 조회`);
```
