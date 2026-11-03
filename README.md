# Board — 회원·게시판 서비스

멀티에이전트 오케스트레이션으로 레거시 게시판을 다시 만든 프로젝트입니다.

먼저 기존 레거시 board 코드를 리버스 엔지니어링해서 요구사항정의서 같은 산출물을 거꾸로 뽑았습니다. 그 산출물을 기준으로 Claude Code 멀티에이전트 오케스트레이션을 써서 프로젝트를 새로 구현했습니다. 이후에는 같은 방식으로 리팩토링까지 진행했습니다.

> "멀티에이전트 오케스트레이션 기반 실무 개발과 AI 기능 구현" 과정에서 진행했습니다.

## 진행 과정

```
레거시 board 코드
   │  ① 리버스 엔지니어링
   ▼
산출물 (요구사항정의서 32건: 기능 19 · 데이터 2 · 인터페이스 1 · 보안 5 · 비기능 5)
   │  ② 멀티에이전트 오케스트레이션으로 재구현
   ▼
새 Board 프로젝트 (이 저장소)
   │  ③ 영향도 분석 → 특성화 테스트 → 리팩토링 → 같은 테스트 무수정 재실행
   ▼
리팩토링 적용본
```

1. **리버스 엔지니어링**: 기존 코드(`MemberController`, `MemberService`, `LoginFilter`, `MemberEntity` 등)를 분석해 요구사항을 역으로 정리했습니다. 요구사항 ID는 `REQ-F-001`처럼 매기고, 코드 주석에 해당 ID를 함께 적어 추적했습니다.
2. **재구현**: 정리한 요구사항을 작업(task) 단위로 나누고 오케스트레이터가 워커를 불러 진행했습니다.
   - `claude-main`: 설계·아키텍처 담당
   - `claude-reviewer`: 결과물 리뷰 담당 (읽기 전용)
   - 서브에이전트: `backend-expert`(코드 영향도 실측), `plan-challenger`(계획 반박), `review-expert`(diff 리뷰)
   - 워커 호출 전에는 승인 게이트를 거치고, 결정·검증 내역은 작업 로그에 남겼습니다.
3. **리팩토링**: 외부 동작은 그대로 두고 내부 구조만 정리했습니다. 동작이 바뀌지 않았다는 건 다음 순서로 확인했습니다.
   - 리팩토링 전에 특성화 테스트를 작성합니다.
   - 리팩토링을 적용합니다.
   - 같은 테스트를 수정 없이 다시 돌립니다. 테스트 파일이 그대로인지는 해시로 대조했습니다.

## 주요 기능

| 영역 | 기능 | 요구사항 |
| --- | --- | --- |
| 회원 | 회원가입, 이메일 중복 확인, 로그인·로그아웃, 회원 목록·상세, 회원 삭제(본인만) | REQ-F-001~009 |
| 게시판 | 자유게시판 목록·상세, 작성·수정·삭제(작성자만) | REQ-F-010, 013, 014 |
| 게시판 | 정렬(최신순·오래된순), 검색(제목·내용·작성자), 페이지 이동 | REQ-F-011, 012, 019 |
| 댓글 | 댓글 작성·수정·삭제 | REQ-F-015 |
| 보안 | 세션 기반 인증 필터, BCrypt 비밀번호 해시, HttpOnly·SameSite 쿠키 | REQ-SE-001~005 |
| 공통 | 서버 측 입력 검증, 403·404 안내 화면 | REQ-NF-001, 002 |

## 기술 스택

- Java 17, Spring Boot 2.7.8
- Spring Web MVC, Spring Data JPA, Thymeleaf, Bean Validation
- spring-security-crypto (BCrypt만 사용, Spring Security는 넣지 않음)
- H2 (기본 실행·테스트), MySQL (`mysql` 프로필)
- Gradle 8.5, JUnit 5

## 실행 방법

기본 실행은 내장 DB(H2 파일 모드)를 사용합니다. 데이터는 홈 폴더의 `~/.board-h2/`에 저장됩니다.

```bash
./gradlew bootRun
```

브라우저에서 http://localhost:8080 으로 접속합니다.

MySQL로 실행하려면 `mysql` 프로필을 켜고 접속 정보를 환경변수로 넘깁니다.

```bash
./gradlew bootJar
DB_URL='jdbc:mysql://localhost:3306/board?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8' \
DB_USER=root DB_PASSWORD=<비밀번호> \
java -jar build/libs/board-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql
```

테스트는 다음 명령으로 실행합니다.

```bash
./gradlew test
```

> JDK 21에서도 빌드됩니다. 빌드 시 `options.release = 17`로 컴파일하고, Boot 2.7.8에 기본으로 들어 있는 byte-buddy가 JDK 21을 지원하지 않아 1.14.9로 올렸습니다.

## URL 구성

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| GET | `/` | 메인 (로그인 상태 표시) |
| GET·POST | `/member/join` | 회원가입 |
| POST | `/member/email-check` | 이메일 중복 확인 |
| GET·POST | `/member/login` | 로그인 |
| POST | `/member/logout` | 로그아웃 |
| GET | `/member/list`, `/member/{id}` | 회원 목록·상세 |
| POST | `/member/delete/{id}` | 회원 삭제 |
| GET | `/board/list` | 게시글 목록 (`sort`, `type`, `keyword`, 페이지 파라미터) |
| GET | `/board/{id}` | 게시글 상세 |
| GET·POST | `/board/write` | 게시글 작성 |
| GET·POST | `/board/{id}/edit` | 게시글 수정 |
| POST | `/board/{id}/delete` | 게시글 삭제 |
| POST | `/board/{boardId}/comments` | 댓글 작성 |
| POST | `/comments/{commentId}/edit`, `/comments/{commentId}/delete` | 댓글 수정·삭제 |

## 프로젝트 구조

```
src/main/java/com/team1/board/
├── member/   회원 (컨트롤러·서비스·엔티티·폼)
├── board/    게시글·댓글 (목록 정렬·검색·페이징 포함)
├── common/   로그인 필터, 예외 처리, 세션 상수, 리다이렉트 경로
└── config/   필터 등록 (WebConfig)
src/main/resources/
├── templates/  Thymeleaf 화면 (member, board, error)
└── static/     CSS
src/test/java/com/team1/board/   통합·단위 테스트 (IntegrationTestSupport 기반)
```

## 리팩토링 결과

리팩토링 가이드북 기준(RF-01, RF-05, RF-07 등)으로 후보를 뽑고, 아이템마다 영향도(호출처·테스트·화면)를 분석한 뒤 적용했습니다.

| 아이템 | 내용 | 결과 |
| --- | --- | --- |
| A-01 (RF-01) | `BoardController.detail`의 항상 참인 분기 제거 | 적용 |
| A-02 (RF-05) | `CommentController`의 리다이렉트 5곳을 `redirectToPost`로 통합 | 적용 |
| A-03 (RF-05) | 로그인·로그아웃·탈퇴의 세션 무효화를 `invalidateSession`으로 통합 | 적용 |
| A-04 (RF-07) | 제목·본문 최대 길이를 `TITLE_MAX_LENGTH`, `CONTENT_MAX_LENGTH` 상수로 통합 | 적용 |
| A-05 (RF-08) | `CommentEntity.getBoardId()` 추가 | 보류 (적용 시 기동 실패를 테스트가 잡아서 되돌림) |

- 테스트: 리팩토링 전 177건 → 후 200건, 실패 0건
- 리팩토링 대상 메서드 라인 커버리지: 97.1% (목표 80%)
- 특성화 테스트 파일 해시: 9/9 일치 (리팩토링 중 테스트를 고치지 않음)
