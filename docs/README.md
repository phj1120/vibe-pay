# VibePay 문서 가이드

## 📁 문서 구조

### 1. `plan.md` - 전체 전략 및 구조
**목적**: 리버스 엔지니어링 및 재구축의 전체 전략, 개념, 구조를 설명

**포함 내용**:
- 현황 분석
- 핵심 전략 (Phase 분리, 도메인별 개발 등)
- 문서 구조 (requirements/ 디렉토리 구성)
- Phase별 상세 설명 (Phase 1, 2, 3)
- 체크리스트
- 성공 기준

**사용 시점**:
- 전체 계획을 이해하고 싶을 때
- 각 Phase의 목적과 구조를 파악할 때
- 요구사항 문서 구조를 확인할 때

---

### 2. `prompts.md` - 실행용 프롬프트 모음
**목적**: 실제로 Claude에게 복붙할 프롬프트만 모아놓은 문서

**포함 내용**:
- Phase 0: 리버스 엔지니어링 프롬프트
- Phase 1: 데이터 레이어 생성 프롬프트
- Phase 2-1 ~ 2-5: 각 도메인별 프롬프트 (Member, Product, RewardPoints, Payment, Order)
- Phase 3: 통합 테스트 프롬프트
- 각 프롬프트별 검증 포인트

**사용 시점**:
- **실제 작업을 시작할 때** (가장 중요!)
- 위에서 아래로 순서대로 프롬프트를 복붙
- 각 단계 완료 후 검증 체크리스트 확인

---

### 3. `requirements/` - 요구사항 문서 (Phase 0 실행 후 생성됨)
**목적**: 리버스 엔지니어링으로 추출된 구체적인 요구사항

**생성될 문서**:
- `phase1-data-layer.md`: 전체 데이터 레이어 명세
- `phase2-member-domain.md`: Member 도메인 요구사항
- `phase2-product-domain.md`: Product 도메인 요구사항
- `phase2-rewardpoints-domain.md`: RewardPoints 도메인 요구사항
- `phase2-payment-domain.md`: Payment 도메인 요구사항 (가장 복잡)
- `phase2-order-domain.md`: Order 도메인 요구사항
- `data-requirements.md`: 초기 데이터 및 샘플 데이터 정의
- `test-scenarios.md`: 통합 테스트 시나리오

**사용 시점**:
- Phase 0 완료 후 생성됨
- Phase 1~3 실행 시 각 문서를 참조
- 재구축할 프로젝트의 "설계 문서" 역할

---

## 🚀 사용 방법

### Step 1: 전체 계획 이해
```
docs/plan.md를 읽고 전체 전략을 이해
```

### Step 2: 리버스 엔지니어링 시작
```
1. docs/prompts.md 열기
2. "Phase 0: 리버스 엔지니어링" 프롬프트 복사
3. Claude에게 붙여넣기
4. docs/requirements/ 디렉토리에 8개 문서 생성됨
```

### Step 3: 재구축 실행
```
1. docs/prompts.md의 Phase 1 프롬프트부터 순서대로 복붙
2. 각 Phase 완료 후 검증 포인트 체크
3. Phase 2는 도메인별로 진행 (Member → Product → RewardPoints → Payment → Order)
4. Phase 3에서 통합 테스트
```

---

## 📝 문서 간 관계

```
plan.md (전략 및 구조 설명)
   ↓
   참조
   ↓
prompts.md (실행용 프롬프트) ← 실제 작업 시 이것만 사용!
   ↓
   실행
   ↓
requirements/ (생성된 요구사항 문서)
   ↓
   참조
   ↓
재구축된 프로젝트
```

---

## 💡 핵심 요약

### 읽을 때
- `plan.md`: 전체 계획 이해

### 실행할 때
- `prompts.md`: 프롬프트 복붙

### 참조할 때
- `requirements/`: 구체적인 요구사항 확인

---

**가장 중요**: 실제 작업 시에는 `docs/prompts.md`만 열고 위에서 아래로 순서대로 따라가면 됩니다!
