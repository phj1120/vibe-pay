# 시퀀스 정의 및 사용 가이드

## 시퀀스 목록

| 시퀀스명 | 용도 | 형식 | 자릿수 | 사용 테이블 |
| --- | --- | --- | --- | --- |
| SEQ_BASKET_NO | 장바구니번호 | 단순 시퀀스 | 15자리 | basket_base |
| SEQ_ORDER_NO | 주문번호 | 날짜+O+시퀀스 | 8+1+6=15자리 | order_base |
| SEQ_CLAIM_NO | 클레임번호 | 날짜+C+시퀀스 | 8+1+6=15자리 | order_detail |
| SEQ_PAY_NO | 결제번호 | 단순 시퀀스 | 15자리 | pay_base |
| SEQ_PAY_INTERFACE_NO | 결제인터페이스번호 | 단순 시퀀스 | 15자리 | pay_interface_log |
| SEQ_MEMBER_NO | 회원번호 | 단순 시퀀스 | 15자리 | member_base |
| SEQ_POINT_HISTORY_NO | 포인트기록번호 | 단순 시퀀스 | 15자리 | point_history |
| SEQ_GOODS_NO | 상품번호 | G+시퀀스 | 1+14=15자리 | goods_base |

## 시퀀스 사용 방법

### 1. basket_no (장바구니번호)

**생성 SQL:**
```sql
LPAD(NEXTVAL('SEQ_BASKET_NO')::TEXT, 15, '0')
```

**예시 결과:**
```
000000000000001
000000000000002
000000000000003
```

**Mapper 메서드:**
```java
String generateBasketNo(); // BasketBaseTrxMapper
```

---

### 2. order_no (주문번호)

**생성 SQL:**
```sql
TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || 'O' || LPAD(NEXTVAL('SEQ_ORDER_NO')::TEXT, 6, '0')
```

**예시 결과:**
```
20251027O000001
20251027O000002
20251027O999999
20251027O000001  -- 최대값 도달 시 자동 순환 (CYCLE)
20251028O000001  -- 날짜가 바뀌면 자연스럽게 구분됨
```

**Mapper 메서드:**
```java
String generateOrderNo(); // OrderBaseTrxMapper
```

**특징:**
- CYCLE 옵션으로 999999 도달 시 자동으로 1부터 재시작
- 날짜(YYYYMMDD)가 포함되어 있어 날짜별로 자연스럽게 구분
- 일일 최대 999,999건 처리 가능

---

### 3. claim_no (클레임번호)

**생성 SQL:**
```sql
TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || 'C' || LPAD(NEXTVAL('SEQ_CLAIM_NO')::TEXT, 6, '0')
```

**예시 결과:**
```
20251027C000001
20251027C000002
20251027C999999
20251027C000001  -- 최대값 도달 시 자동 순환 (CYCLE)
20251028C000001  -- 날짜가 바뀌면 자연스럽게 구분됨
```

**Mapper 메서드:**
```java
String generateClaimNo(); // OrderBaseTrxMapper
```

**특징:**
- CYCLE 옵션으로 999999 도달 시 자동으로 1부터 재시작
- 날짜(YYYYMMDD)가 포함되어 있어 날짜별로 자연스럽게 구분
- 일일 최대 999,999건 처리 가능

---

### 4. pay_no (결제번호)

**생성 SQL:**
```sql
LPAD(NEXTVAL('SEQ_PAY_NO')::TEXT, 15, '0')
```

**예시 결과:**
```
000000000000001
000000000000002
000000000000003
```

**Mapper 메서드:**
```java
String generatePayNo(); // PayBaseTrxMapper
```

---

### 5. pay_interface_no (결제인터페이스번호)

**생성 SQL:**
```sql
LPAD(NEXTVAL('SEQ_PAY_INTERFACE_NO')::TEXT, 15, '0')
```

**예시 결과:**
```
000000000000001
000000000000002
000000000000003
```

**Mapper 메서드:**
```java
String generatePayInterfaceNo(); // PayInterfaceLogTrxMapper
```

---

### 6. member_no (회원번호)

**생성 SQL:**
```sql
LPAD(NEXTVAL('SEQ_MEMBER_NO')::TEXT, 15, '0')
```

**예시 결과:**
```
000000000000001
000000000000002
000000000000003
```

**Mapper 메서드:**
```java
String generateMemberNo(); // MemberBaseTrxMapper
```

---

### 7. point_history_no (포인트기록번호)

**생성 SQL:**
```sql
LPAD(NEXTVAL('SEQ_POINT_HISTORY_NO')::TEXT, 15, '0')
```

**예시 결과:**
```
000000000000001
000000000000002
000000000000003
```

**Mapper 메서드:**
```java
String generatePointHistoryNo(); // PointHistoryTrxMapper (추후 구현)
```

---

### 8. goods_no (상품번호)

**생성 SQL:**
```sql
'G' || LPAD(NEXTVAL('SEQ_GOODS_NO')::TEXT, 14, '0')
```

**예시 결과:**
```
G00000000000001
G00000000000002
G00000000000003
```

**Mapper 메서드:**
```java
String generateGoodsNo(); // GoodsBaseTrxMapper (추후 구현)
```

---

## 서비스 레이어 사용 예시

### 장바구니 등록
```java
@Service
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {
    private final BasketBaseTrxMapper basketBaseTrxMapper;

    @Transactional(value = "rwdbTxManager")
    public void createBasket(BasketRequest request) {
        // 시퀀스로 번호 생성
        String basketNo = basketBaseTrxMapper.generateBasketNo();

        BasketBase basketBase = new BasketBase();
        basketBase.setBasketNo(basketNo);
        basketBase.setMemberNo(request.getMemberNo());
        basketBase.setGoodsNo(request.getGoodsNo());
        basketBase.setItemNo(request.getItemNo());
        basketBase.setQuantity(request.getQuantity());
        basketBase.setIsOrder(false);

        basketBaseTrxMapper.insertBasketBase(basketBase);
    }
}
```

### 주문 등록
```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderBaseTrxMapper orderBaseTrxMapper;

    @Transactional(value = "rwdbTxManager")
    public String createOrder(OrderRequest request) {
        // 날짜 포함 주문번호 생성
        String orderNo = orderBaseTrxMapper.generateOrderNo();
        // 결과 예: 20251027O000001

        OrderBase orderBase = new OrderBase();
        orderBase.setOrderNo(orderNo);
        orderBase.setMemberNo(request.getMemberNo());

        orderBaseTrxMapper.insertOrderBase(orderBase);

        return orderNo;
    }
}
```

---

## 시퀀스 관리

### 현재 값 조회
```sql
SELECT CURRVAL('SEQ_BASKET_NO');
SELECT CURRVAL('SEQ_ORDER_NO');
SELECT CURRVAL('SEQ_CLAIM_NO');
SELECT CURRVAL('SEQ_PAY_NO');
SELECT CURRVAL('SEQ_PAY_INTERFACE_NO');
SELECT CURRVAL('SEQ_MEMBER_NO');
SELECT CURRVAL('SEQ_POINT_HISTORY_NO');
SELECT CURRVAL('SEQ_GOODS_NO');
```

### 다음 값 조회
```sql
SELECT NEXTVAL('SEQ_BASKET_NO');
SELECT NEXTVAL('SEQ_ORDER_NO');
SELECT NEXTVAL('SEQ_CLAIM_NO');
SELECT NEXTVAL('SEQ_PAY_NO');
SELECT NEXTVAL('SEQ_PAY_INTERFACE_NO');
SELECT NEXTVAL('SEQ_MEMBER_NO');
SELECT NEXTVAL('SEQ_POINT_HISTORY_NO');
SELECT NEXTVAL('SEQ_GOODS_NO');
```

### 시퀀스 수동 초기화 (필요 시)
```sql
-- 일반적으로 초기화 불필요 (CYCLE 옵션으로 자동 순환)
-- 특별한 경우에만 수동 초기화
ALTER SEQUENCE SEQ_ORDER_NO RESTART WITH 1;
ALTER SEQUENCE SEQ_CLAIM_NO RESTART WITH 1;
ALTER SEQUENCE SEQ_BASKET_NO RESTART WITH 1;
```

---

## 주의사항

1. **날짜 포함 시퀀스 (order_no, claim_no)**
   - 매일 자정에 시퀀스 초기화 필요
   - CYCLE 옵션으로 최대값 초과 시 자동 순환
   - 배치 작업 실패 시 번호 중복 가능성 있음

2. **단순 시퀀스**
   - 초기화 불필요
   - 최대값(999999999999999) 도달 시 오류 발생

3. **성능 최적화**
   - CACHE 20 설정으로 성능 향상
   - 동시성 환경에서도 안전

4. **트랜잭션**
   - NEXTVAL()은 트랜잭션 롤백 시에도 증가
   - 번호 건너뛰기 발생 가능 (정상 동작)
