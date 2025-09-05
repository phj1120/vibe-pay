# VibePay 프론트엔드 명세서

## 기술 스택

- **프레임워크**: Nuxt.js 3
- **UI 라이브러리**: Vuetify 3
- **언어**: TypeScript
- **스타일링**: CSS, Vuetify 테마
- **아이콘**: Material Design Icons

## 프로젝트 구조

```
vibe-pay-frontend/
├── app.vue                 # 루트 앱 컴포넌트
├── nuxt.config.ts         # Nuxt 설정
├── layouts/
│   └── default.vue        # 기본 레이아웃 (네비게이션 포함)
├── pages/
│   ├── index.vue          # 홈페이지 (목차)
│   ├── members/
│   │   ├── index.vue      # 회원 목록
│   │   └── [id].vue       # 회원 상세/수정
│   ├── products/
│   │   ├── index.vue      # 상품 목록
│   │   └── [id].vue       # 상품 상세/수정
│   └── order/
│       ├── index.vue      # 주문서 생성
│       └── popup.vue      # 결제 팝업
├── plugins/
│   └── inicis.client.ts   # 이니시스 SDK 로드
└── public/
    └── favicon.ico
```

## 페이지 명세

### 1. 홈페이지 (`/`)
**파일**: `pages/index.vue`

**기능**:
- 프로젝트 소개
- 네비게이션 메뉴 안내
- 사용법 가이드

**컴포넌트**:
- `v-card`: 프로젝트 소개 카드

### 2. 회원 관리

#### 2.1 회원 목록 (`/members`)
**파일**: `pages/members/index.vue`

**기능**:
- 회원 목록 조회 (API: `GET /api/members`)
- 회원 등록 버튼
- 회원 상세 페이지 이동
- 주문하기 버튼

**컴포넌트**:
- `v-data-table`: 회원 목록 테이블
- `v-btn`: 새 회원 등록, 주문하기 버튼
- `v-skeleton-loader`: 로딩 상태

**테이블 컬럼**:
- ID, 이름, 주소, 전화번호, 가입일, 액션

#### 2.2 회원 상세/수정 (`/members/[id]`)
**파일**: `pages/members/[id].vue`

**기능**:
- 회원 정보 조회/수정 (API: `GET/PUT /api/members/{id}`)
- 적립금 관리 (API: `PUT /api/rewardpoints/add`)
- 주문 내역 조회 (API: `GET /api/orders/member/{memberId}`)
- 주문 취소 (API: `POST /api/orders/{id}/cancel`)

**컴포넌트**:
- `v-form`: 회원 정보 입력 폼
- `v-text-field`: 입력 필드들
- `v-data-table`: 주문 내역 테이블
- `v-btn`: 저장, 취소, 적립금 추가, 주문 취소 버튼

**라우트 파라미터**:
- `id`: 회원 ID (new인 경우 신규 등록)

### 3. 상품 관리

#### 3.1 상품 목록 (`/products`)
**파일**: `pages/products/index.vue`

**기능**:
- 상품 목록 조회 (API: `GET /api/products`)
- 상품 등록 버튼
- 상품 상세 페이지 이동

**컴포넌트**:
- `v-data-table`: 상품 목록 테이블
- `v-btn`: 새 상품 등록 버튼

**테이블 컬럼**:
- ID, 상품명, 가격

#### 3.2 상품 상세/수정 (`/products/[id]`)
**파일**: `pages/products/[id].vue`

**기능**:
- 상품 정보 조회/수정 (API: `GET/PUT /api/products/{id}`)

**컴포넌트**:
- `v-form`: 상품 정보 입력 폼
- `v-text-field`: 상품명, 가격 입력
- `v-btn`: 저장, 취소 버튼

### 4. 주문서 시스템

#### 4.1 주문서 생성 (`/order`)
**파일**: `pages/order/index.vue`

**기능**:
- 회원 선택 (자동완성)
- 상품 선택 및 수량 조절
- 적립금 사용
- 금액 계산
- PG사 결제 프로세스

**컴포넌트**:
- `v-autocomplete`: 회원/상품 선택
- `v-data-table`: 주문 상품 목록
- `v-text-field`: 수량, 적립금 입력
- `v-card`: 주문 요약
- `form`: 이니시스 결제 폼 (숨김)

**주요 기능**:
1. **회원 선택**: 드롭다운에서 회원 선택
2. **상품 추가**: 드롭다운에서 상품 선택 시 주문 목록에 추가
3. **수량 조절**: 각 상품별 수량 조절 가능
4. **적립금 사용**: 회원 보유 적립금 범위 내 사용
5. **금액 계산**: 실시간 총액 계산
6. **결제 프로세스**: 
   - 1단계: 결제 파라미터 생성 (`POST /api/payments/initiate`)
   - 2단계: 이니시스 SDK 로드 및 결제창 호출
   - 3단계: 결제 승인 (`POST /api/payments/confirm`)
   - 4단계: 주문 생성 (`POST /api/orders`)

**쿼리 파라미터**:
- `memberId`: 미리 선택된 회원 ID

#### 4.2 결제 팝업 (`/order/popup`)
**파일**: `pages/order/popup.vue`

**기능**:
- 결제 진행 중 로딩 화면
- 직접 접근 시 경고 메시지

**컴포넌트**:
- 로딩 스피너
- 안내 메시지
- 창 닫기 버튼

## 레이아웃

### 기본 레이아웃 (`layouts/default.vue`)

**구성**:
- `v-navigation-drawer`: 사이드 네비게이션
- `v-main`: 메인 콘텐츠 영역

**네비게이션 메뉴**:
- 홈 (`/`)
- 회원 관리 (`/members`)
- 상품 관리 (`/products`)
- 주문서 (`/order`)

## 플러그인

### 이니시스 SDK (`plugins/inicis.client.ts`)

**기능**:
- 이니시스 결제 SDK 자동 로드
- 클라이언트 사이드에서만 실행

## API 연동

### 프록시 설정
```typescript
// nuxt.config.ts
vite: {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
}
```

### API 호출 패턴
```typescript
// 예시: 회원 목록 조회
const fetchMembers = async () => {
  try {
    const response = await fetch('/api/members')
    if (!response.ok) throw new Error('Failed to fetch members')
    return await response.json()
  } catch (error) {
    console.error(error)
    // 에러 처리
  }
}
```

## 상태 관리

### 반응형 데이터
- `ref()`: 단일 값 상태
- `computed()`: 계산된 속성
- `watch()`: 상태 변화 감지

### 예시 상태 관리
```typescript
// 주문서 상태
const selectedMember = ref(null)
const orderItems = ref([])
const usedPoints = ref(0)

// 계산된 속성
const subtotal = computed(() => 
  orderItems.value.reduce((acc, item) => acc + (item.quantity * item.price), 0)
)

const total = computed(() => 
  Math.max(0, subtotal.value - usedPoints.value)
)
```

## 스타일링

### 테마 설정
```typescript
// nuxt.config.ts
vuetify: {
  vuetifyOptions: {
    theme: {
      defaultTheme: 'dark'
    }
  }
}
```

### CSS 클래스
- Vuetify 컴포넌트 클래스 활용
- 커스텀 스타일은 `<style scoped>` 사용
- Material Design Icons 사용

## 에러 처리

### API 에러 처리
- try-catch 블록으로 에러 캐치
- 사용자에게 알림 메시지 표시
- 콘솔에 에러 로그 기록

### 사용자 피드백
- 로딩 상태 표시
- 성공/실패 메시지
- 확인 다이얼로그

## 성능 최적화

### 코드 스플리팅
- Nuxt.js 자동 코드 스플리팅 활용
- 페이지별 번들 분리

### 이미지 최적화
- 적절한 이미지 포맷 사용
- 지연 로딩 적용

### API 호출 최적화
- 불필요한 API 호출 방지
- 캐싱 전략 적용
