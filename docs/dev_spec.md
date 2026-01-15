# [Tech Spec] Kakao Meeting Culture Coach

## 1. Tech Stack
- **Language:** Kotlin (JDK 17+)
- **Framework:** Spring Boot 3.x
- **Database:** MySQL 8.0 (JPA/Hibernate)
- **AI Pipeline (2-Step):**
    - **Step 1 (Ear):** GPT-4o Audio (STT & 화자 분리)
    - **Step 2 (Brain):** GPT-4o (or GPT-5.2 Preview) (심층 성향 분석 & 코칭 생성)
- **Async:** Spring `@Async` (Local Thread Pool)

## 2. Architecture & Design Pattern
**Simplified Hexagonal Architecture** (Web -> Service -> Domain <- Infra)

### Hackathon Critical Constraints
1.  **Single User Mode:** 인증 로직 없음. 모든 요청은 `User ID = 1`로 처리.
2.  **Hybrid Data Storage:**
    - 메타데이터(제목, 날짜, 상태) -> **RDB Column**
    - 복잡한 분석 결과(코칭, 케미 슬라이더) -> **JSON String (TEXT Type)**
3.  **Static UI Strategy:**
    - 레이더 차트, 막대 그래프 등 복잡한 시각화 요소는 프론트엔드 구현 비용 절감을 위해 **서버가 정적 이미지 URL을 반환**한다.

## 3. Database Schema

### `users` (사전 정의 데이터 - Seeding)
- **Purpose:** 사용자 정보 및 **ARRC 성향**.
- **Columns:**
    - `id` (PK)
    - `nickname` (ex: "루시", "제피")
    - `profile_image_url`
    - `arrc_type` (VARCHAR): 성향 코드 (ex: `TYPE_ANALYTIC`, `TYPE_SUPPORTIVE`)
    - `character_badge_label` (VARCHAR): UI 표시용 라벨 (ex: "죠르디 특성", "소심형")
    - `strength_tags`: (ex: "논리,두괄식")
    - `weakness_tags`: (ex: "공감,리액션")

### `meetings`
- **Columns:**
    - `id`, `title`, `audio_file_url`
    - `status` (PENDING, COMPLETED, FAILED)
    - `summary` (3줄 요약)
    - `overview_image_url` (상단 차트 더미 이미지 URL)
    - `created_at` (BaseTimeEntity) -> **정렬 기준**

### `meeting_participants`
- **Columns:**
    - `meeting_id`, `user_id`, `speaker_label` ("Speaker A")
    - `ai_analysis_summary` (TEXT) -> **[중요]** 챗봇이 읽을 해당 화자의 태도 요약 (Hidden Data).

### `meeting_relationships` (Core - JSON Storage)
- **Purpose:** '나'와 상대방의 케미 분석 및 코칭 데이터.
- **Columns:**
    - `meeting_id`, `from_user_id` (나), `to_user_id` (상대)
    - `chemistry_score` (Int)
    - `analysis_result_json` (TEXT): API 응답용 전체 JSON 덩어리.
        - 포함 내용: 성향 차이 슬라이더 값, 소통 가이드(Chips), 상세 코칭 카드(상황/스크립트/조언).

## 4. Implementation Details
### Service Logic (Sorting)
- **MeetingRepository:**
    - `findAllByUserIdOrderByCreatedAtDesc(Long userId)`
    - **Rule:** 목록 조회 시 반드시 **최신순(DESC)**으로 정렬하여 반환해야 함.

### AI Pipeline (Sequential Async)
`MeetingService.analyze()`:
1.  **Context Loading:** DB에서 참여자들의 `arrc_type`, `tags` 정보를 가져옴.
2.  **Transcribe (Step 1):** `OpenAIAdapter.transcribe(audio)` -> **Script** 생성.
3.  **Consult (Step 2):** `OpenAIAdapter.consult(script, user_profiles)`
    - Prompt: "User 'Lucy' is Shy Type, 'Jepy' is Analytic Type. Analyze their conflict using Kakao 8 Principles."
4.  **Save:** 결과 JSON을 `meeting_relationships`에 저장.

## 5. API & Security
- **Auth:** None. (Always `userId = 1`)
- **Controller:** 비즈니스 로직 없이 Service 호출 및 DTO 변환만 담당.
