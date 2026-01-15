# [Product Spec] Kakao Meeting Culture Coach (BetterTalk)

## 1. Project Overview
- **Project Name:** Kakao Meeting Culture Coach (BetterTalk)
- **Goal:** 카카오 8대 원칙(Kakao Style)과 참여자 성향(ARRC)을 기반으로 회의를 진단하고, 동료와의 관계(Chemistry)를 코칭하는 AI 서비스.
- **Value Proposition:**
    - **관계 중심 분석:** 단순 요약을 넘어, "나와 동료의 케미"를 분석하고 갈등 원인을 성향 차이에서 찾음.
    - **실전 코칭:** 추상적인 조언 대신, 구체적인 수정 대본(Rewriting)과 행동 가이드 제공.
- **Hackathon Strategy (Lite Mode):**
    - **Single User Mode:** 복잡한 로그인 없이 **'나(User ID: 1)'**를 기준으로 모든 데이터가 동작.
    - **Hybrid UI:** 복잡한 그래프는 정적 이미지로 대체하되, **AI 코칭 텍스트의 퀄리티**와 **챗봇의 맥락 이해**에 집중.

## 2. Core Logic: Assessment Criteria
### A. The 8 Principles (Kakao Style)
AI는 다음 8가지 원칙을 기준으로 대화의 태도를 평가한다.
1. **Act for Kakao** (조직 우선)
2. **Solve User Problems** (사용자 가치)
3. **No Reliance on Names** (수평적 소통/신충헌)
4. **No Limits by Experience** (경험을 넘은 해결책)
5. **Aim High** (본질적 해결)
6. **Speak Honestly** (솔직함과 신뢰)
7. **Collaborate across Boundaries** (협업)
8. **Respect & Commit** (결정 존중)

### B. User Personality (ARRC Model)
- 사용자의 성향을 사전에 정의된 **ARRC 유형**으로 분류하여 DB에 미리 저장해둔다. (User Table)
- AI는 이 성향 정보를 컨텍스트로 받아 "왜 이 두 사람이 부딪혔는지" 원인을 심층 분석한다.

## 3. User Experience Flow
### Step 1: 접속 (Dashboard)
- 서비스 접속 시, **가장 최근에 완료된 회의 분석 결과**가 메인 화면에 즉시 표시된다.
- 좌측 사이드바에는 내가 참여한 회의 목록이 **최신순**으로 나열된다.

### Step 2: 회의 등록 (Upload)
- 녹음 파일을 업로드하면 백그라운드에서 분석이 시작된다.
- 시스템은 별도 설정 없이 **'나(참여자 1)'와 '동료들'**을 자동으로 구분한다.

### Step 3: 결과 리포트 (Insight)
- **성향 캐릭터:** 나(루시-소심형)와 상대(제피-죠르디 특성)의 캐릭터 매핑 시각화.
- **케미 분석:**
    - **Score:** 13점 (아직 쿵짝이 안 맞아요).
    - **Traits Slider:** 직설 vs 쿠션, 부정 vs 긍정 등 성향 차이 그래프.
    - **Communication Guide:** "제피님은 데이터 근거를 좋아해요" (Action Item).
    - **Script Refinement:** 문제가 된 발언을 "카카오스럽게" 고쳐주는 Before & After 카드.

### Step 4: 맥락 챗봇 (Interactive)
- "아까 제피가 왜 기분 나빠했어?"라고 물으면, **회의 내용 + 제피의 성향(ARRC) + 카카오 원칙**을 종합하여 답변한다.

## 4. Success Metrics
- **자동 노출:** 접속 시 최신 회의가 바로 로딩되는가?
- **화자/성향 매핑:** AI가 '나'와 '상대방'의 성향을 고려하여 충돌 원인을 설명하는가?
- **솔루션 구체성:** "말을 예쁘게 하세요"가 아니라 "두괄식으로 이렇게 바꿔보세요"라는 구체적 대안을 주는가?
