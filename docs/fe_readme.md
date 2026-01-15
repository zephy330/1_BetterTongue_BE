# Bettertalk Frontend

회의 소통 분석 및 케미 점수 대시보드 - 팀원 간의 소통 패턴을 분석하고 더 나은 협업을 위한 인사이트를 제공합니다.

> **🚧 해커톤 프로젝트**: 제한된 시간 내 개발로 일부 기능은 이미지/하드코딩으로 대체되어 있습니다.

---

## 🔌 API 연동 가이드 (서버 개발자용)

해커톤 기간 동안 **실제 서버 연동이 필요한 부분**과 **이미지/하드코딩으로 대체하는 부분**을 구분해두었습니다.

### 📷 이미지/하드코딩 처리 (서버 연동 불필요)

| 컴포넌트 | 위치 | 처리 방식 |
|----------|------|-----------|
| 강한 스킬 / 부족한 스킬 | `UserProfileCard`, `ChemistryAnalysis` | 고정 이미지 또는 하드코딩된 값 |
| 회의 영향력 분석 | `InfluenceAnalysis` | 레이더 차트 고정 이미지 |
| 성향 차이 (직설↔쿠션어 등) | `ChemistryScore` | 하드코딩된 위치값 |

### 🔗 실제 서버 연동 필요 (LLM 반환값)

| 데이터 | 응답 형식 | 사용 컴포넌트 | 비고 |
|--------|-----------|---------------|------|
| **케미 점수** | `{ score: number, message: string }` | `ChemistryScore` | 예: `{ score: 13, message: "아직 쿵짝이 안 맞아요" }` |
| **소통 방법 팁** | `string[]` (2~3개) | `ChemistryScore` | 예: `["자신의 의견을 피력할 때 망설이지 않아요", "명확한 규칙이나 기준을 좋아해요"]` |
| **전체 대화 요약문** | `string` | `ScenarioCard` | 시나리오 description + analysis 필드 |
| **카나의 한마디** | `string` | `ScenarioCard`, `RightChatPanel` | AI 어시스턴트의 조언 메시지 |

### 📡 예상 API 응답 구조

```typescript
// POST /api/analyze-meeting
interface AnalyzeMeetingResponse {
  // 케미 점수
  chemistry: {
    score: number          // 0-100
    message: string        // 점수에 대한 한줄 설명
  }
  
  // 소통 팁 (2~3개)
  communicationTips: string[]
  
  // 대화 요약
  summary: {
    description: string    // 상황 설명
    analysis: string       // 분석 내용
  }
  
  // AI 어시스턴트 메시지
  aiAdvice: string
}
```

### 🗂 Mock 데이터 위치

서버 연동 전까지 `constants/mock-data.ts`에서 더미 데이터를 관리합니다:

```typescript
import { chemistryScore, scenarios, aiAssistant } from "@/constants"

// chemistryScore.score    → 케미 점수
// chemistryScore.tips     → 소통 방법 팁
// scenarios[0].description → 대화 요약
// aiAssistant.greeting    → 카나의 한마디
```

---

## 📋 목차

- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [페이지 구성](#페이지-구성)
- [컴포넌트 가이드](#컴포넌트-가이드)
- [타입 정의](#타입-정의)
- [상수 및 Mock 데이터](#상수-및-mock-데이터)
- [디자인 시스템](#디자인-시스템)

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Next.js 16 (App Router) |
| Language | TypeScript |
| Styling | Tailwind CSS v4 |
| UI Components | Radix UI + shadcn/ui |
| Icons | Lucide React |
| Font | Geist (Sans & Mono) |
| Analytics | Vercel Analytics |

---

## 🚀 시작하기

### 요구사항

- Node.js 18+
- npm / yarn / pnpm

### 설치 및 실행

```bash
# 의존성 설치
npm install

# 개발 서버 실행 (http://localhost:3000)
npm run dev

# 프로덕션 빌드
npm run build

# 프로덕션 서버 실행
npm start

# 린트 검사
npm run lint
```

---

## 📁 프로젝트 구조

```
bettertalk_fe/
├── app/                    # Next.js App Router
│   ├── layout.tsx          # 루트 레이아웃 (메타데이터, 폰트 설정)
│   ├── page.tsx            # 메인 대시보드 페이지
│   └── globals.css         # 전역 스타일 및 CSS 변수
│
├── components/             # React 컴포넌트
│   ├── ui/                 # shadcn/ui 기본 컴포넌트
│   │   └── button.tsx      # 버튼 컴포넌트 (variant, size 지원)
│   │
│   ├── shared/             # 공통 재사용 컴포넌트
│   │   ├── index.ts        # 공통 컴포넌트 export
│   │   ├── skill-bar.tsx   # 스킬 진행 바
│   │   ├── avatar-badge.tsx # 아바타 뱃지
│   │   ├── compact-card.tsx # 컴팩트 카드
│   │   └── trait-scale.tsx  # 성향 비교 스케일
│   │
│   ├── left-sidebar.tsx    # 왼쪽 사이드바 (네비게이션)
│   ├── main-content.tsx    # 메인 컨텐츠 영역
│   ├── right-chat-panel.tsx # 오른쪽 채팅 패널 (AI 어시스턴트)
│   │
│   ├── user-profile-card.tsx   # 사용자 프로필 카드
│   ├── influence-analysis.tsx  # 회의 영향력 분석 (레이더 차트)
│   ├── chemistry-analysis.tsx  # 케미 분석 섹션
│   ├── chemistry-score.tsx     # 케미 점수 및 성향 비교
│   ├── communication-tips.tsx  # 소통 팁 섹션
│   └── scenario-card.tsx       # 시나리오 카드
│
├── types/                  # TypeScript 타입 정의
│   └── index.ts            # 공통 타입 (Props, 데이터 모델)
│
├── constants/              # 상수 및 Mock 데이터
│   ├── index.ts            # 상수 export
│   ├── colors.ts           # 색상 상수 정의
│   └── mock-data.ts        # 개발용 Mock 데이터
│
├── lib/                    # 유틸리티 함수
│   └── utils.ts            # cn() - Tailwind 클래스 병합 유틸
│
├── public/                 # 정적 파일 (이미지, 아이콘)
│
├── components.json         # shadcn/ui 설정
├── tailwind.config.ts      # Tailwind 설정
├── tsconfig.json           # TypeScript 설정
└── package.json            # 의존성 및 스크립트
```

---

## 📄 페이지 구성

### 메인 대시보드 (`/`)

3단 레이아웃으로 구성된 소통 분석 대시보드:

```
┌─────────────┬─────────────────────────────────┬──────────────────┐
│             │                                 │                  │
│   Left      │        Main Content             │   Right Chat     │
│   Sidebar   │                                 │   Panel          │
│   (180px)   │        (flex-1)                 │   (320px)        │
│             │                                 │                  │
│  - 로고      │  - 회의 정보 헤더                 │  - AI 어시스턴트   │
│  - 녹음 버튼  │  - 참석자 목록                   │  - 대화형 질문     │
│  - 회의 목록  │  - 프로필 카드                   │  - 채팅 입력       │
│             │  - 영향력 분석                    │                  │
│             │  - 케미 분석                      │                  │
│             │  - 시나리오 카드                  │                  │
│             │                                 │                  │
└─────────────┴─────────────────────────────────┴──────────────────┘
```

---

## 🧩 컴포넌트 가이드

### 공통 컴포넌트 (`components/shared/`)

#### `SkillBar`
스킬 진행률을 시각적으로 표시하는 바 컴포넌트

```tsx
import { SkillBar } from "@/components/shared"

<SkillBar label="논리" value={70} color={colors.cyan} />
```

#### `AvatarBadge`
원형 프로필 이미지와 이름을 표시하는 컴포넌트

```tsx
import { AvatarBadge } from "@/components/shared"

<AvatarBadge 
  name="제피" 
  image="/zephy.jpg" 
  bg="#ffde26" 
  selected={true}
  size="md"  // "sm" | "md" | "lg"
/>
```

#### `CompactCard`
작은 아이콘과 레이블을 표시하는 카드

```tsx
import { CompactCard } from "@/components/shared"

<CompactCard title="베프쬬" image="/jjyo.png" bg="#f9487a" />
```

#### `TraitScale`
두 사용자의 성향 위치를 비교하는 슬라이더

```tsx
import { TraitScale } from "@/components/shared"

<TraitScale 
  left="직설" 
  right="쿠션어" 
  yellowPos={30}  // 제피 위치 (0-100%)
  pinkPos={65}    // 루시 위치 (0-100%)
/>
```

### 페이지 컴포넌트

#### `UserProfileCard`
사용자의 프로디(프로페셔널 버디) 특성을 표시합니다.

```tsx
import UserProfileCard from "@/components/user-profile-card"
import { lucyProfile } from "@/constants"

<UserProfileCard {...lucyProfile} />
```

#### `InfluenceAnalysis`
회의 참석자의 역할별 영향력을 레이더 차트로 시각화합니다.

- **축**: 조율, 분위기, 질문, 아이디어, 논리
- **표시**: 각 참석자의 아바타와 메타볼 효과

#### `ChemistryScore`
두 사용자 간의 케미 점수와 성향 차이를 비교합니다.

### `Button` (shadcn/ui)

```tsx
// 사용 가능한 variants
variant: "default" | "destructive" | "outline" | "secondary" | "ghost" | "link"

// 사용 가능한 sizes
size: "default" | "sm" | "lg" | "icon" | "icon-sm" | "icon-lg"
```

---

## 📝 타입 정의

모든 타입은 `types/index.ts`에서 관리됩니다:

```tsx
import type { 
  SkillBar,
  Participant,
  UserProfileCardProps,
  Meeting,
  AvatarBadgeProps,
  CompactCardProps,
  SkillBarProps,
  TraitScaleProps,
  ChatMessage,
  Scenario
} from "@/types"
```

### 주요 타입

| 타입 | 설명 |
|------|------|
| `Participant` | 참석자 정보 (id, name, avatar, color) |
| `Meeting` | 회의 정보 (id, title, date, participants) |
| `SkillBar` | 스킬 바 데이터 (label, value, color) |
| `UserProfileCardProps` | 사용자 프로필 카드 Props |
| `Scenario` | 시나리오 데이터 |

---

## 📦 상수 및 Mock 데이터

### 색상 상수 (`constants/colors.ts`)

```tsx
import { colors, participantColors } from "@/constants"

// 사용 예시
colors.cyan       // "#30ffee"
colors.pink       // "#f9487a"
colors.yellow     // "#ffde26"
colors.textMuted  // "#9199b6"

// 참석자별 색상
participantColors.lucy   // "#f9487a"
participantColors.zephy  // "#ffde26"
participantColors.james  // "#30ffee"
```

### Mock 데이터 (`constants/mock-data.ts`)

```tsx
import { 
  participants,      // 참석자 데이터
  currentMeeting,    // 현재 회의 정보
  meetingList,       // 회의 목록
  lucyProfile,       // 루시 프로필
  chemistrySkills,   // 케미 스킬 데이터
  chemistryScore,    // 케미 점수 데이터
  scenarios,         // 시나리오 목록
  aiAssistant,       // AI 어시스턴트 정보
} from "@/constants"
```

---

## 🎨 디자인 시스템

### 색상 팔레트

| 변수명 | 색상 | 용도 |
|--------|------|------|
| `--background` | `#08090d` | 메인 배경 |
| `--foreground` | `#f7f7f8` | 기본 텍스트 |
| `--primary` | `#30ffee` | 주요 액션, 강조 |
| `--chart-1` | `#30ffee` | 시안 (제임스) |
| `--chart-2` | `#f9487a` | 핑크 (루시) |
| `--chart-3` | `#ffde26` | 노랑 (제피) |
| `--card` | `#131720` | 카드 배경 |
| `--secondary` | `#292d3d` | 보조 요소 |
| `--muted-foreground` | `#9199b6` | 보조 텍스트 |

### 참석자별 테마 컬러

| 참석자 | 색상 | Hex | 상수 |
|--------|------|-----|------|
| 루시 | 핑크 | `#f9487a` | `colors.pink` |
| 제피 | 노랑 | `#ffde26` | `colors.yellow` |
| 제임스 | 시안 | `#30ffee` | `colors.cyan` |

---

## 🔧 개발 가이드

### 새 컴포넌트 추가 시

1. `types/index.ts`에 Props 타입 정의
2. 색상은 `constants/colors.ts`의 상수 사용
3. 재사용 가능하면 `components/shared/`에 배치
4. 접근성(a11y) 속성 추가 (`aria-label`, `role` 등)

### API 연동 시

1. `constants/mock-data.ts`의 데이터 구조를 API 응답 형식으로 사용
2. 타입은 `types/index.ts`에서 import

---

## 📚 참고 자료

- [Next.js Documentation](https://nextjs.org/docs)
- [Tailwind CSS v4](https://tailwindcss.com/docs)
- [Radix UI](https://www.radix-ui.com/)
- [shadcn/ui](https://ui.shadcn.com/)
- [Lucide Icons](https://lucide.dev/)
