# API & DB 설계 가이드

## 1. API 설계 원칙

### 1.1 RESTful API

본 프로젝트는 RESTful API 원칙을 따릅니다.

#### HTTP Methods

| Method | 용도             | 예시                     |
| ------ | ---------------- | ------------------------ |
| GET    | 리소스 조회      | `GET /api/users/{id}`    |
| POST   | 리소스 생성      | `POST /api/users`        |
| PUT    | 리소스 전체 수정 | `PUT /api/users/{id}`    |
| PATCH  | 리소스 부분 수정 | `PATCH /api/users/{id}`  |
| DELETE | 리소스 삭제      | `DELETE /api/users/{id}` |

#### URL 설계 규칙

- 소문자 사용
- 명사 복수형 사용 (`/users`, `/orders`)
- 계층 구조는 `/`로 표현
- 하이픈(`-`) 사용, 언더스코어(`_`) 지양

```
Good: /api/users/123/orders
Bad:  /api/getUser?id=123
```

---

## 2. 공통 응답 형식

### 2.1 Response Wrapper

모든 API 응답은 JSON 형식 및 공통 Wrapper 형식을 사용합니다.

#### 성공 응답 (예시)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com"
  },
  "error": null
}
```

#### 실패 응답 (예외처리 : RFC 9457를 따름)

```json
{
  "success": false,
  "data": null,
  "error": {
    "type": "https://api.example.com/errors/user-not-found",
    "title": "User Not Found",
    "status": 404,
    "detail": "ID가 1인 사용자를 찾을 수 없습니다.",
    "instance": "/api/users/1",
    "timestamp": "2025-01-15T10:30:00Z"
  }
}
```

#### Slice 응답 형식

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "게시글 1",
        "createdAt": "2025-01-15T10:00:00"
      },
      {
        "id": 2,
        "title": "게시글 2",
        "createdAt": "2025-01-15T09:00:00"
      }
    ],
    "pageable": {
      "page": 0,
      "size": 20,
      "sort": "createdAt,desc"
    },
    "hasNext": true
  },
  "error": null
}
```

## 3. 예외 처리 (RFC 9457)

### 3.1 RFC 9457 기반 에러 응답

**RFC 9457 (Problem Details for HTTP APIs)** 표준을 따릅니다.

#### 에러 응답 구조

```json
{
  "success": false,
  "data": null,
  "error": {
    "type": "https://api.example.com/errors/user-not-found",
    "title": "User Not Found",
    "status": 404,
    "detail": "ID가 123인 사용자를 찾을 수 없습니다.",
    "instance": "/api/users/123",
    "timestamp": "2025-01-15T10:30:00Z"
  }
}
```

---

## 4. API 버전 관리

### 4.1 URI 버전 관리

```
/api/v1/users
/api/v2/users
```

### 4.2 버전 관리 원칙

- 하위 호환성이 깨지는 변경 시 메이저 버전 증가
- 신규 필드 추가는 기존 버전 유지
- Deprecated API는 최소 기간 유지 후 제거

---

## 5. 데이터베이스 설계 원칙

### 5.1 명명 규칙

#### 테이블명

- **snake_case** 사용
- 복수형 사용
- 예: `users`, `order_items`, `product_categories`

#### 컬럼명

- **snake_case** 사용
- 명확하고 설명적인 이름
- 예: `user_id`, `created_at`, `is_active`

#### 인덱스명

```
idx_{table}_{column}
예: idx_users_email, idx_orders_user_id
```

#### 외래키명

```
fk_{table}_{reference_table}
예: fk_orders_users, fk_order_items_orders
```

### 5.2 인덱스 전략

#### 인덱스가 필요한 경우

- 외래키 컬럼
- 자주 조회되는 컬럼 (email, username 등)
- ORDER BY, GROUP BY에 사용되는 컬럼
- WHERE 조건에 자주 사용되는 컬럼

#### 복합 인덱스

```sql
-- 좋은 예: 자주 함께 조회되는 컬럼
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- 나쁜 예: 선택도가 낮은 컬럼을 앞에 배치
CREATE INDEX idx_orders_status_user ON orders(status, user_id);
```

### 5.3 연관관계 매핑

#### 양방향 연관관계 지양

- 단방향 연관관계 우선 고려
- 꼭 필요한 경우에만 양방향 설정

#### Lazy Loading 기본 사용

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

#### N+1 문제 방지

```java
// Fetch Join 사용
@Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
Optional<Order> findByIdWithUser(@Param("id") Long id);
```

---

## 6. 참고 자료

- [RFC 9457 - Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html)
- [RESTful API Design Best Practices](https://restfulapi.net/)

---
