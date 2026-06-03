# Issue Tracking System

SE 2026 Spring Term Project - Team 1

## Team Members

| Department | Name |
|------------|------|
| 소프트웨어학부 | 김지수 |
| 소프트웨어학부 | 박서윤 |
| 소프트웨어학부 | 이정주 |
| 소프트웨어학부 | 이준우 |

---

## Project Overview

Issue Tracking System(ITS)은 프로젝트 단위로 이슈를 등록하고 관리하는 시스템입니다.

사용자는 이슈를 생성하고, 담당자를 배정하며, 코멘트를 통해 협업할 수 있습니다. 또한 이슈 상태를 관리하고 통계 분석 및 개발자 추천 기능을 제공합니다.

---

## Main Features

### Account Management
- Admin, PL, Dev, Tester 계정 관리

### Issue Management
- 이슈 등록
- 이슈 검색 및 브라우즈
- 이슈 상세 조회
- 코멘트 추가
- 이슈 배정
- 상태 변경
- 이슈 재오픈

### Statistics
- 상태별 통계
- 우선순위별 통계
- 개발자별 통계
- 날짜별 발생 추이

### Assignee Recommendation
- 해결 이력 기반 개발자 추천
- 텍스트 유사도 기반 추천

---

## Technology Stack

- Java 17
- Gradle
- SQLite
- JDBC
- Swing
- JavaFX
- JUnit 5

---

## Architecture

본 프로젝트는 MVC 및 Layered Architecture를 기반으로 설계하였다.

<pre>
View
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
SQLite
</pre>

### Layer Responsibility

- View : Swing, JavaFX UI
- Controller : 사용자 요청 처리
- Service : 비즈니스 로직 수행
- Repository : 데이터 접근
- SQLite : 데이터 저장

---

## Issue Lifecycle

<pre>
NEW
 ↓
ASSIGNED
 ↓
FIXED
 ↓
RESOLVED
 ↓
CLOSED

REOPENED
 └──→ ASSIGNED
</pre>

---

## Assignee Recommendation

추천 기능은 다음 요소를 기반으로 개발자를 추천한다.

- 개발자별 해결 이슈 수(Fixed Count)
- 현재 이슈와 과거 해결 이슈 간 텍스트 유사도
- Cosine Similarity 기반 점수 계산

<pre>
totalScore =
    fixedCount * 1.0 +
    similarityScore * 5.0
</pre>

---

## Project Deliverables

### 1. 발표 슬라이드
- 최종 프로젝트 발표 자료

### 2. 프로젝트 문서
- 요구사항 분석, 설계, 구현, 테스트 내용을 포함한 최종 보고서

### 3. 소스코드 및 실행 파일
- Java 소스코드
- Gradle 빌드 파일
- SQLite 데이터베이스
- JUnit 테스트 코드

### 4. 프로젝트 소개 동영상
- 프로젝트 설계 설명 및 기능 시연 영상

---

## GitHub Repository

https://github.com/Term-Project-team1/issueTrackingSystem

---

## Build & Run

프로젝트 루트 디렉터리에서 아래 명령어를 실행한다.

### JavaFX UI 실행 (기본)
<pre>
bash ./gradlew run 
</pre>
### Swing UI 실행
<pre>
bash ./gradlew runSwing 
</pre>
### JUnit 테스트 실행
<pre>
bash ./gradlew test 
</pre>
### Windows 환경
<pre>
bash gradlew.bat run gradlew.bat runSwing gradlew.bat test 
</pre>
---

## Project Structure

<pre>
src
 ├─ main
 │   ├─ java
 │   └─ resources
 └─ test
     └─ java
</pre>

- main : 실제 애플리케이션 코드
- test : JUnit 테스트 코드
