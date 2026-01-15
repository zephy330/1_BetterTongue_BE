# [API Spec] Kakao Meeting Culture Coach

## 1. Common Info
- **Base URL:** `/api`
- **User:** All requests assume `Current User ID = 1` (Lucy).
- **Date Format:** `yyyy.MM.dd` or ISO 8601.

---

## 2. Endpoints

### 🟢 POST /api/meetings (Upload)
- **Description:** 회의 녹음 파일 업로드.
- **Req:** `file` (audio), `title` (text)
- **Res:** `202 Accepted`

### 🟢 GET /api/meetings (List)
- **Description:** 좌측 사이드바용 회의 목록.
- **Logic:** **최신순(Created At Desc)으로 정렬**되어 반환됨.
- **Client Action:** 앱 접속 시 이 리스트를 호출하고, `list[0].id` (가장 최신)를 사용하여 상세 API를 자동 호출할 것.
- **Res:**
```json
[
  {
    "id": 123,
    "title": "대시보드 수정 아젠다 (가장 최신)",
    "date": "2026.01.10",
    "summary": "팝업 개편 논의..."
  },
  {
    "id": 122,
    "title": "UT 시나리오 테스트",
    "date": "2026.01.09",
    "summary": "사용자 테스트 결과..."
  }
]
