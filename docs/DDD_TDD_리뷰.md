# DDD & TDD 적용 리뷰

## 1. 적용 요약

| 구분 | 적용 전 | 적용 후 |
|------|---------|---------|
| **DDD** | Controller → Repository 직통 | Controller → **Application Service** → Repository |
| **TDD** | 테스트 없음 | Domain / Application / Controller 단위·슬라이스 테스트 |

---

## 2. DDD 적용 내용

### 2.1 레이어 구조

```
[기존]
PostController ──────────────────────► PostRepository
                  (직접 호출)              UserRepository

[변경]
PostController ──► PostApplicationService ──► PostRepository
     │                        │                    UserRepository
     │                        │
     ▼                        ▼
  PostForm              CreatePostCommand
                        UpdatePostCommand
```

### 2.2 추가된 컴포넌트

| 패키지 | 클래스 | 역할 |
|--------|--------|------|
| `application/post/` | `PostApplicationService` | Use Case 오케스트레이션, 트랜잭션 경계 |
| `application/post/` | `CreatePostCommand` | 게시글 생성 입력 DTO |
| `application/post/` | `UpdatePostCommand` | 게시글 수정 입력 DTO |

### 2.3 Application Service 메서드

| 메서드 | 설명 | 트랜잭션 |
|--------|------|----------|
| `getPosts(Pageable)` | 페이징 목록 조회 | readOnly |
| `getPost(Long)` | 단건 조회 | readOnly |
| `createPost(CreatePostCommand)` | 게시글 생성 | readWrite |
| `updatePost(Long, UpdatePostCommand)` | 게시글 수정 | readWrite |
| `deletePost(Long)` | 게시글 삭제 | readWrite |

### 2.4 DDD 원칙 적용 상황

| 원칙 | 적용 | 비고 |
|------|------|------|
| 계층 분리 | ✅ | Presentation / Application / Domain 분리 |
| Use Case 캡슐화 | ✅ | Application Service에 집중 |
| Command 객체 | ✅ | CreatePostCommand, UpdatePostCommand |
| 도메인 비침해 | ✅ | Domain은 기존 유지, Application에서 조율 |
| Repository 인터페이스 | ✅ | Domain 패키지에 유지 (JPA 구현) |

---

## 3. TDD 적용 내용

### 3.1 테스트 구조

```
src/test/java/com/wedding/board/
├── domain/
│   ├── post/PostTest.java          # Post 엔티티 단위 테스트
│   └── user/UserTest.java          # User 엔티티 단위 테스트
├── application/
│   └── post/PostApplicationServiceTest.java  # Application Service 단위 (Mock)
└── web/
    ├── PostControllerTest.java     # Controller 슬라이스 (@WebMvcTest)
    └── TestSecurityUtils.java      # 테스트용 인증 헬퍼
```

### 3.2 테스트 유형별 전략

| 레이어 | 테스트 방식 | Mock/실제 |
|--------|-------------|-----------|
| **Domain** | 순수 단위 테스트 | 없음 (POJO) |
| **Application** | Mockito Extension | PostRepository, UserRepository Mock |
| **Controller** | @WebMvcTest | PostApplicationService Mock, Security 비활성화 |

### 3.3 테스트 케이스 목록 (총 17개)

**Domain (Post) - 2개**
- `create`: 제목, 내용, 작성자로 생성, createdAt=updatedAt 검증
- `update`: 제목·내용 수정, updatedAt 갱신 검증

**Domain (User) - 1개**
- `create`: 아이디, 암호화된 비밀번호로 생성

**Application Service - 7개**
- `getPosts`: 페이징 목록 반환 + Repository 호출 verify
- `getPost`: 단건 조회 + Repository 호출 verify
- `getPost_notFound`: 미존재 시 예외
- `createPost`: 생성 후 id 반환
- `createPost_userNotFound`: 미존재 사용자 시 예외
- `updatePost`: 수정
- `deletePost`: 삭제

**Controller - 7개**
- `list`: 목록 조회 + Service 호출 verify
- `detail`: 상세 조회 + Service 호출 verify
- `createForm`: 작성 폼 표시
- `create`: 생성 후 redirect + Service 호출 verify
- `create_validationError`: 유효성 실패 시 폼 반환 + attributeHasErrors 검증
- `update`: PUT 수정 후 redirect + Service 호출 verify
- `deletePost`: 삭제 후 redirect + Service 호출 verify

### 3.4 테스트 개선 사항 (적용됨)

- `@AfterEach`로 SecurityContext 정리 (테스트 격리)
- 모든 Controller 테스트에 `verify()`로 Service 호출 검증
- `create_validationError`에 `attributeHasErrors("postForm")` 추가
- Post `create` 테스트 통합 (중복 제거)
- Post `update` 테스트에 `updatedAt` 갱신 검증 강화

### 3.5 TDD 전략

- **Domain**: 비즈니스 규칙 검증 (빠르고 격리)
- **Application**: Use Case 흐름 검증 (Mock으로 의존성 제거)
- **Controller**: HTTP 요청/응답 검증 (슬라이스로 빠른 실행)

---

## 4. 개선 포인트 (향후)

| 항목 | 현재 | 제안 |
|------|------|------|
| Value Object | 없음 | PostId, Title 등 고려 |
| Domain Event | 없음 | PostCreated 등 이벤트 발행 |
| 통합 테스트 | 없음 | @SpringBootTest + 실제 DB |
| Security 테스트 | 비활성화 | 보안 필터 포함 테스트 |
| 작성자 권한 | 미검증 | 본인 글만 수정/삭제 |

---

## 5. 실행 방법

```bash
# 테스트
.\gradlew.bat test

# 애플리케이션
.\gradlew.bat bootRun
```
