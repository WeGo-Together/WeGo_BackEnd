```bash
           .---.            ,----..
          /. ./|           /   /   \
      .--'.  ' ;          |   :     :    ,---.
     /__./ \ : |          .   |  ;. /   '   ,'\
 .--'.  '   \' .   ,---.  .   ; /--`   /   /   |
/___/ \ |    ' '  /     \ ;   | ;  __ .   ; ,. :
;   \  \;      : /    /  ||   : |.' .''   | |: :
 \   ;  `      |.    ' / |.   | '_.' :'   | .; :
  .   \    .\  ;'   ;   /|'   ; : \  ||   :    |
   \   \   ' \ |'   |  / |'   | '/  .' \   \  /
    :   '  |--" |   :    ||   :    /    `----'
     \   \ ;     \   \  /  \   \ .'
      '---"       `----'    `---`
```

<div align="center">

# WeGo-Together Backend

**모임 기반 커뮤니티 플랫폼 백엔드 서비스**

</div>

---

## 1. 프로젝트 개요

### 한 줄 설명
사용자들이 다양한 모임을 찾고, 참여하고, 관리할 수 있도록 돕는 **모임 기반 커뮤니티 플랫폼** 백엔드 API 서버입니다.

### 주요 기능/목적

| 기능 | 설명 |
|------|------|
| **로그인/회원가입** | 이메일 기반 회원가입, Google OAuth 소셜 로그인 |
| **모임 관리** | 모임 생성, 조회(목록/상세), 참가/취소, 수정, 삭제 |
| **사용자 프로필** | 프로필 조회/수정, 프로필 이미지 변경, MBTI 정보 관리 |
| **팔로우 시스템** | 팔로우/언팔로우, 팔로우/팔로워 목록 조회 |
| **실시간 알림** | SSE(Server-Sent Events) 기반 실시간 알림, 읽음 처리 |
| **실시간 채팅** | WebSocket 기반 실시간 채팅, DM, 그룹 채팅방 관리 |
| **이미지 처리** | AWS S3 업로드, 썸네일 자동 생성, 이미지 관리 |
| **모임 태그** | 모임 분류 및 태그 기반 검색 |

---

## 2. 기술 스택

### 언어 및 프레임워크

| 구분 | 기술 | 버전 |
|------|------|------|
| **언어** | Java | 21 |
| **프레임워크** | Spring Boot | 3.5.8 |
| **빌드 도구** | Gradle | 8.x |

### 주요 라이브러리

| 카테고리 | 라이브러리 | 버전 | 용도 |
|----------|------------|------|------|
| **보안** | Spring Security | - | 인증/인가 |
| | JJWT | 0.12.6 | JWT 토큰 처리 |
| **데이터** | Spring Data JPA | - | ORM |
| | QueryDSL (Jakarta) | 5.1.0 | 동적 쿼리 |
| | MySQL Connector | latest | DB 드라이버 |
| **캐싱** | Spring Data Redis (Lettuce) | - | 캐시/세션 |
| **실시간 통신** | Spring WebSocket | - | 채팅 |
| | SSE (Servlet) | - | 알림 |
| **클라우드** | AWS SDK S3 | 2.39.6 | 이미지 저장소 |
| **이미지 처리** | Thumbnailator | 0.4.20 | 썸네일 생성 |
| | TwelveMonkeys ImageIO | 0.1.6 | WebP 지원 |
| **API 문서** | Springdoc OpenAPI | 2.7.0 | Swagger UI |
| **모니터링** | Spring Actuator | - | 헬스 체크 |
| | Micrometer Prometheus | - | 메트릭 수집 |
| **유틸리티** | Lombok | latest | 보일러플레이트 제거 |

### 인프라 (Docker)

| 서비스 | 이미지 | 용도 |
|--------|--------|------|
| Redis | redis:7-alpine | 세션/캐시 |
| Prometheus | prom/prometheus | 메트릭 수집 |
| Grafana | grafana/grafana | 모니터링 대시보드 |

---

## 3. 프로젝트 구조

```
src/main/java/team/wego/wegobackend/
├── auth/                    # 인증 모듈 (로그인, 회원가입, OAuth)
├── user/                    # 사용자 모듈 (프로필, 팔로우)
├── group/                   # 모임 모듈 (V1, V2)
│   └── v2/                  # 개선된 모임 관리 (QueryDSL, Redis 캐시)
├── chat/                    # 채팅 모듈 (WebSocket)
├── notification/            # 알림 모듈 (SSE)
├── image/                   # 이미지 처리 모듈
├── tag/                     # 태그 모듈
├── infrastructure/          # 외부 서비스 연동 (AWS, OAuth)
└── common/                  # 공통 모듈
    ├── config/              # 설정 (CORS, Swagger, QueryDSL)
    ├── security/            # 보안 (JWT, Security Config)
    ├── exception/           # 전역 예외 처리
    └── response/            # 통일된 API 응답
```

---

## 4. 환경 변수

### 환경 변수 목록

`.env.example` 파일을 참고하여 `.env` 파일을 생성하세요.

| 변수명 | 설명 | 예시 |
|--------|------|------|
| **RDS_ENDPOINT** | MySQL 데이터베이스 호스트 주소 | `your-rds-endpoint.rds.amazonaws.com` |
| **RDS_USER** | 데이터베이스 사용자명 | `dbuser` |
| **RDS_PASSWORD** | 데이터베이스 비밀번호 | `your-password` |
| **S3_ENDPOINT** | S3 버킷 퍼블릭 엔드포인트 | `your-bucket.s3.ap-northeast-2.amazonaws.com` |
| **S3_ACCESS_KEY_ID** | AWS IAM Access Key ID | `AKIAXXXXXXXX` |
| **S3_SECURE_ACCESS_KEY** | AWS IAM Secret Access Key | `your-secret-key` |
| **JWT_SECRET** | JWT 서명용 비밀키 (Base64 인코딩) | `your-base64-encoded-secret` |
| **GOOGLE_CLIENT_ID** | Google OAuth 클라이언트 ID | `xxxxx.apps.googleusercontent.com` |
| **GOOGLE_CLIENT_SECRET** | Google OAuth 클라이언트 시크릿 | `GOCSPX-xxxxx` |
| **GOOGLE_REDIRECT_URI** | Google OAuth 리다이렉트 URI | `http://localhost:3000/google-auth-test` |

### 환경 변수 설정 방법

```bash
# 1. .env.example 파일을 복사하여 .env 파일 생성
cp .env.example .env

# 2. .env 파일을 열어 실제 값으로 수정
# 각 환경 변수에 적절한 값을 입력하세요
```

---

## 5. API 문서

### Swagger UI
서버 실행 후 아래 URL에서 API 문서를 확인할 수 있습니다:
- **로컬**: `http://localhost:8080/swagger-ui.html`
- **운영**: `https://api.wego.monster/swagger-ui.html`

### 주요 API 엔드포인트

#### 인증 (Auth) - `/api/v1/auth`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/signup` | 회원가입 |
| POST | `/login` | 로그인 |
| POST | `/google` | Google OAuth 로그인 |
| POST | `/logout` | 로그아웃 |
| DELETE | `/withdraw` | 회원탈퇴 |
| POST | `/refresh` | Access Token 재발급 |

#### 사용자 (User) - `/api/v1/users`
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/me` | 내 프로필 조회 |
| GET | `/{userId}` | 특정 사용자 프로필 조회 |
| PATCH | `/profile-image` | 프로필 이미지 변경 |
| PATCH | `/profile` | 프로필 정보 수정 |
| POST | `/follow` | 팔로우 요청 |
| DELETE | `/unfollow` | 팔로우 취소 |
| GET | `/{userId}/follow` | 팔로우 목록 조회 |
| GET | `/{userId}/follower` | 팔로워 목록 조회 |

#### 모임 (Group) - `/api/v1/groups`, `/api/v2/groups`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/create` | 모임 생성 |
| GET | `/` | 모임 목록 조회 (커서 기반 페이징) |
| GET | `/{groupId}` | 모임 상세 조회 |
| PATCH | `/{groupId}` | 모임 정보 수정 |
| DELETE | `/{groupId}` | 모임 삭제 |
| POST | `/{groupId}/attend` | 모임 참가 |
| POST | `/{groupId}/cancel` | 모임 참가 취소 |
| GET | `/me` | 내 모임 조회 (ATTENDING, HOSTED, INTERESTED) |

#### 채팅 (Chat) - `/api/v1/chat`
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/rooms` | 내 채팅방 목록 조회 |
| GET | `/rooms/{roomId}` | 채팅방 상세 조회 |
| GET | `/rooms/{roomId}/messages` | 메시지 이력 조회 |
| POST | `/rooms` | DM 채팅방 생성 |
| PUT | `/rooms/{roomId}/read` | 읽음 처리 |
| DELETE | `/rooms/{roomId}` | 채팅방 나가기 |

**WebSocket 연결**: `ws://host/ws-chat?token={accessToken}`

#### 알림 (Notification) - `/api/v1/notifications`
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/subscribe` | SSE 연결 (실시간 알림) |
| GET | `/` | 알림 목록 조회 |
| GET | `/unread-count` | 읽지 않은 알림 개수 |
| POST | `/{notificationId}/read` | 단건 읽음 처리 |
| POST | `/read-all` | 전체 읽음 처리 |

#### 이미지 (Image) - `/api/v1/images`
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/upload` | 이미지 업로드 |
| GET | `/{imageId}` | 이미지 조회 |
| DELETE | `/{imageId}` | 이미지 삭제 |

#### 모니터링 - `/actuator` (관리 포트: 9091)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/health` | 헬스 체크 |
| GET | `/info` | 애플리케이션 정보 |
| GET | `/prometheus` | Prometheus 메트릭 |

---

## 6. 실행 방법

### 사전 요구사항
- Java 21
- Docker & Docker Compose
- MySQL 8.x (또는 AWS RDS)

### 로컬 실행

```bash
# 1. 의존성 설치 및 빌드
./gradlew clean build

# 2. Docker 서비스 실행 (Redis, Prometheus, Grafana)
docker-compose up -d

# 3. 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Docker Compose 서비스
```bash
# 모든 서비스 실행
docker-compose up -d

# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f

# 서비스 중지
docker-compose down
```

---

## 7. 가이드 문서

프로젝트 관련 상세 문서는 `docs/` 폴더에서 확인할 수 있습니다:

| 문서 | 설명 |
|------|------|
| [API_DB_설계_규율.md](docs/API_DB_설계_규율.md) | API 및 데이터베이스 설계 규칙 |
| [기술_스택_가이드.md](docs/기술_스택_가이드.md) | 기술 스택 선정 이유 및 가이드 |
| [코드_컨벤션.md](docs/코드_컨벤션.md) | 코드 작성 규칙 |
| [채팅서비스_아키텍처.md](docs/채팅서비스_아키텍처.md) | 채팅 시스템 아키텍처 설명 |
| [채팅서비스_클라이언트_가이드문서.md](docs/채팅서비스_클라이언트_가이드문서.md) | 채팅 클라이언트 연동 가이드 |

---
## 8. 유지보수 계획

### 모니터링 시스템 적용 및 고도화
- 메트릭 등 시스템 주요 지표 모니터링 대상 항목 설계
- APM Tool 등 Spring Actuator, Prometheus 기본 메트릭 외 추가 지표 도입
- 설계된 항목 기반 Grafana 커스텀 대시보드 적용
- 주요 지표 기준점 초과 등으로 인한 경고 발생 시 알림 적용 (Discord 연동)
- 로그 중앙화 시스템 구축 (ELK Stack 또는 Loki)

### 소스코드 리팩토링
- Group V1 → V2 완전 마이그레이션 및 V1 레거시 코드 제거
- 도메인 이벤트 패턴 도입으로 모듈 간 결합도 감소
- 공통 예외 처리 고도화 및 에러 코드 체계화
- 테스트 코드 커버리지 향상 (목표: 80% 이상)
- 불필요한 N+1 쿼리 제거 및 Fetch Join 최적화

### API 성능 테스트 및 개선 작업
- 부하 테스트 도구 도입 (k6, JMeter, Gatling)
- 주요 API 응답 시간 기준 설정 및 측정 (P95, P99)
- Slow Query 모니터링 및 인덱스 최적화
- Redis 캐싱 전략 고도화 (리프레시 토큰, 모임 목록, 사용자 프로필 등)
- 커넥션 풀 튜닝 (HikariCP, Lettuce)
- API Rate Limiting 적용

### 보안 강화
- 보안 취약점 점검 (OWASP Top 10 대응)
- API 입력값 검증 강화 및 SQL Injection 방어
- Refresh Token Rotation 구현
- 민감 정보 암호화 저장 (개인정보 등)

### CI/CD 파이프라인 개선
- GitHub Actions 기반 파이프라인 개선 (코드 스타일 체크 등)
- 환경별 설정 분리 및 Secret 관리 체계화

### 문서화
- API 변경 이력 관리 (Changelog)
- Swagger 문서 보완 (요청/응답 예시, 에러 코드)
