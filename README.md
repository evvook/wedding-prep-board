# 결혼 준비 게시판 (Wedding Prep Board)

최소 2인용 로그인이 가능한 웹 게시판입니다.

## 요구사항

- **JDK 8 이상** (JRE가 아닌 JDK 필요 - `javac` 포함)
- Gradle Wrapper 포함 (gradlew.bat 사용)

## 실행 방법

```bash
# Windows
.\gradlew.bat bootRun

# 실행 후 브라우저에서 http://localhost:8080 접속
```

## 테스트 계정

| 아이디 | 비밀번호 |
|--------|----------|
| user1  | 1234     |
| user2  | 1234     |

## 기능

- **로그인/로그아웃**: Spring Security 기반
- **게시글 목록**: 페이징 (10개씩)
- **게시글 작성/수정/삭제**: 로그인 사용자만 가능
- **게시글 조회**: 비로그인 사용자도 가능

## 프로젝트 구조

```
src/main/java/com/wedding/board/
├── config/          # Security, Data 초기화
├── domain/          # User, Post 엔티티
├── security/        # UserDetails 구현
└── web/             # Controller, Form
```

## 기술 스택

- Spring Boot 2.7
- Spring Security
- Spring Data JPA
- H2 (인메모리 DB)
- Thymeleaf + Bootstrap 5
