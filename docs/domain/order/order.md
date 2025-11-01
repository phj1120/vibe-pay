## 주문/결제

### 개발 요청사항

결제 구현 시 전략 패턴을 활용해 구현

결제

- `pay_way_code`:  카드, 포인트(`PAY002` )
- `pg_type_code`: 이니시스, 나이스 (`PAY005`)

공통코드(enum)가 있다면 해당 코드를 기반으로 개발 진행.

### 현재 구현의 제한 사항

배송비와 쿠폰 관련 업무는 현재 고려하지 않을 것.

상품의 재고에 대한 동시성 문제 고려하지 않을 것.

주문시 포인트 적립 고려하지 않을 것.

주문 완료에 대한 알림 기능 고려하지 않을 것.

주문상태(`order_status_code`)의 진행에 대한 프로세스는 고려하지 않을 것.

시퀀스가 CYCLE 로 돌아 PK 오류 나는 것에 대해서는 고려하지 않을 것.

### 설정 값

application.yml

```yaml
payment:
  inicis:
    mid: "INIpayTest"
    apiKey: "ItEQKi3rY7uvDS8l"
    sign-key: "SU5JTElURV9UUklQTEVERVNfS0VZU1RS"
    return-url: "http://localhost:3000/api/order/payment/return"
    close-url: "http://localhost:3000/order/close"
    gopaymethod: "Card"
    acceptmethod: "below1000"

  nice:
    mid: "nicepay00m"
    merchant-key: "EYzu8jGGMfqaDEp76gSckuvnaHHu+bC4opsSN6lHv3b2lurNYkVXrZ7Z1AoqQnXI3eLuaUFyoRNC6FkrzVjceg=="
    return-url: "http://localhost:3000/api/order/payment/return"
    cancel-url: "http://localhost:3000/order/cancel"
```

**returnUrl 변경 사항:**
- 기존: `/order/return` (SSR 페이지)
- 변경: `/api/order/payment/return` (Next.js API Route)
- 이유: PG 응답을 API Route에서 처리하고 postMessage로 부모 창에 전달하는 방식으로 변경

### 주문 프로세스

```yaml
(화면) 결제하기 버튼 클릭
	→ (서버) 주문번호조회 `/api/order/generateOrderNumber`
	→ (서버) 요청 정보 생성 `/api/payments/initiate` 
	→ 쿠키에 주문 정보, 요청 정보 생성
	→ 결제 처리 팝업창 호출 `/order/popup` 
		나이스: `width=570,height=830,scrollbars=yes,resizable=yes`
		이니시스: `width=840,height=600,scrollbars=yes,resizable=yes`
		→ 쿠키에 있는 주문 정보, 요청 정보 꺼내와 PG사별 form 에 세팅 후 쿠키 삭제
		→ 해당 응답 값에 선택 된 PG 사에 따라서 각각에 맞는 정보 세팅해 PG 창 호출
			나이스: `window.goPay(document.nicePayForm);`
			이니시스: `window.INIStdPay.pay('inicisForm');`
		→ PG 창에서 결제 처리
			나이스: 
				- 인증 완료 후 `nicepaySubmit()` 콜백 함수 호출
				- 콜백에서 인증 응답이 append된 form을 `/api/order/payment/return`으로 submit
			이니시스:
				- 인증 완료 후 returnUrl로 직접 리다이렉트
		→ (FO API Route) 결제 응답 처리 (`/api/order/payment/return`)
			※ Next.js API Route로 구현 (SSR이 아닌 API 엔드포인트)
			
			**PG사별 인증 응답 전달 방식:**
			- 나이스: `application/x-www-form-urlencoded` (FormData)
			- 이니시스: `application/x-www-form-urlencoded` (FormData)
			
			**PG사 판별 방법 (Content-Type이 아닌 데이터 필드로 판별):**
			- 이니시스: `resultCode` 필드 존재
			- 나이스: `AuthResultCode` 필드 존재
			
			**결제 성공 여부 판단:**
			- 이니시스: `resultCode === '0000'`
			- 나이스: `AuthResultCode === '0000'`
			
			**응답 처리:**
			1. PG 응답 데이터 파싱 및 PG사 판별
			2. 결제 성공/실패 여부 확인
			3. 결제 결과를 담은 HTML 페이지 반환 (postMessage 포함)
			4. HTML 페이지에서 `window.opener.postMessage()`로 부모 창에 결과 전송
			5. postMessage 전송 후 1초 뒤 팝업 창 자동 닫기
			
		결제처리 결과를 부모창에서 수신하고 이후 주문 프로세스 진행
		(인증 응답에 승인 요청 정보가 있어, 이를 서버에서 주문 시점에 호출)
		
	→ (서버) 주문 `/api/orders/order`
	
	**주문 성공/실패 결과 처리:**
	- 결제 성공 + 주문 성공 → 주문 완료 페이지 (`/order/complete?orderNo={orderNumber}`)
	- 결제 실패 → 주문 실패 모달 (상세 오류 정보 포함) → 장바구니로 이동
	- 결제 성공 + 주문 실패 → 주문 실패 모달 (결제 완료 안내 + 고객센터 안내) → 장바구니로 이동
```

- 결제처리창 팝업이 열릴 경우, 주문서의 결제하기 버튼을 비활성화한다.
- 결제처리창 팝업이 닫힌 경우 주문서의 결제하기 버튼을 활성화한다.
- 쿠키는 5분 유지 가능하고, 암호화 하지 않는다.
- 결제/주문 실패 시 상세 오류 정보(PG사, 오류 코드, 발생 시각 등)를 모달로 표시한다.

### 프론트엔드 구현 세부사항

```json
#### 1. 결제 처리 흐름 (postMessage 기반)

**파일 구조:**
- `/order/sheet` - 주문서 페이지 (부모 창)
- `/order/popup` - 결제 팝업 페이지 (PG form 제출)
- `/api/order/payment/return` - API Route (PG 응답 처리)

**통신 흐름:**

*이니시스:*
주문서 페이지 (부모 창)
  ↓ window.open()
결제 팝업 (/order/popup)
  ↓ INIStdPay.pay() 호출
이니시스 결제 창
  ↓ 결제 완료 후 returnUrl로 리다이렉트
API Route (/api/order/payment/return)
  ↓ window.opener.postMessage()
주문서 페이지 (결과 수신)
  ↓ 주문 API 호출
주문 완료 or 실패 모달

*나이스:*
주문서 페이지 (부모 창)
  ↓ window.open()
결제 팝업 (/order/popup)
  ↓ goPay() 호출 + nicepaySubmit() 콜백 등록
나이스 결제 창
  ↓ 결제 완료 후 nicepaySubmit() 콜백 호출
결제 팝업 (/order/popup)
  ↓ form.submit() → /api/order/payment/return
API Route (/api/order/payment/return)
  ↓ window.opener.postMessage()
주문서 페이지 (결과 수신)
  ↓ 주문 API 호출
주문 완료 or 실패 모달

#### 2. postMessage 데이터 구조

interface PaymentResultMessage {
  success: boolean;
  authData?: InicisAuthResponse | NiceAuthResponse;
  error?: string;
  errorDetails?: {
    pgType?: string;          // 'INICIS' | 'NICE'
    errorCode?: string;        // PG 에러 코드
    errorMessage?: string;     // 에러 메시지
    timestamp?: string;        // ISO 8601 형식
  };
}

#### 3. 에러 메시지 형식

**결제 실패 시:**
결제에 실패했습니다

[상세 정보]
PG사: KG이니시스
오류 코드: 0001
발생 시각: 2025. 10. 31. 오전 11:30:45

**주문 생성 실패 시 (결제 성공):**

재고가 부족합니다

[상세 정보]
오류 코드: 3001
주문번호: 20251031O000481
발생 시각: 2025. 10. 31. 오전 11:30:45

결제는 완료되었으나 주문 처리 중 문제가 발생했습니다.
고객센터(주문번호 포함)로 문의해주세요.

#### 4. 결제하기 버튼 상태 관리

- **초기 상태**: 활성화
- **팝업 열림**: 비활성화
- **팝업 닫힘**: 활성화
- **주문 처리 중**: 비활성화 유지
```

### PG 사에 따른 결제 요청 및 응답

- 나이스
    - 결제 요청 Form: `nicePayForm`
        
        | name | `PaymentInitiateResponse` 에 해당하는 값 | 기타 |
        | --- | --- | --- |
        | GoodsName | goodName |  |
        | Amt | amt |  |
        | MID | mid |  |
        | EdiDate | ediDate |  |
        | Moid | moid |  |
        | SignData | signData |  |
        | PayMethod |  | `CARD` 고정 |
        | ReturnURL | returnUrl |  |
        | BuyerName | buyerName |  |
        | BuyerTel | buyerTel |  |
        | ReqReserved |  | 미사용. |
        | BuyerEmail | buyerEmail |  |
        | CharSet |  | `UTF-8` 고정 |
    - 결제 응답: FormData (`application/x-www-form-urlencoded`)
        
        | name | `PaymentConfirmRequest` 에 해당하는 값 | 비고 |
        | --- | --- | --- |
        | AuthResultCode |  | `0000` 만 결제 성공. PG사 판별 키 필드 |
        | AuthResultMsg |  |  |
        | AuthToken | authToken |  |
        | PayMethod |  |  |
        | MID | mid |  |
        | Moid | orderNo |  |
        | Signature | transactionId |  |
        | Amt | amount |  |
        | TxTid | tradeNo |  |
        | NextAppURL | authUrl |  |
        | NetCancelURL | netCancelUrl |  |
- 이니시스
    - 결제 요청 Form: `inicisForm`
        
        | name | `PaymentInitiateResponse` 에 해당하는 값 | 기타 |
        | --- | --- | --- |
        | mid | mid |  |
        | oid | oid |  |
        | price | price |  |
        | timestamp | timestamp |  |
        | signature | signature |  |
        | verification | verification |  |
        | mKey | mKey |  |
        | version | version |  |
        | currency | currency |  |
        | moId | moid |  |
        | goodname | goodName |  |
        | buyertel | buyerTel |  |
        | buyeremail | buyerEmail |  |
        | returnUrl | returnUrl |  |
        | closeUrl | closeUrl |  |
        | gopaymethod | gopaymethod |  |
        | acceptmethod | acceptmethod |  |
        | charset |  | `UTF-8` 고정 |
    - 결제 응답: FormData (`application/x-www-form-urlencoded`)

        | name | `PaymentConfirmRequest` 에 해당하는 값 | 비고 |
        | --- | --- | --- |
        | resultCode |  | `0000` 만 결제 성공. PG사 판별 키 필드 |
        | resultMsg |  |  |
        | mid |  |  |
        | orderNumber | orderNo |  |
        | authToken | authToken |  |
        | idc_name |  |  |
        | authUrl | authUrl |  |
        | netCancelUrl | netCancelUrl |  |
        | charset |  |  |
        | merchantData |  |  |

    결제가 성공 했을 경우 `PaymentConfirmRequest` 에 PG 사 별 위의 값을 세팅해 주문 호출

    **주의**: 이니시스 승인 요청 시 `price` 필드가 필요하므로, 프론트엔드에서 `PaymentConfirmRequest` 생성 시 결제 금액(`finalAmount`)을 `price` 필드에 포함해야 함
    
    - PG사 판별 로직 (API Route에서 처리)
        - Content-Type에 관계없이 응답 데이터의 필드로 판별
        - 이니시스 판별: `resultCode` 필드 존재
        - 나이스 판별: `AuthResultCode` 필드 존재
- 결제 인증 응답 처리 (Next.js API Route)
    - POST `/api/order/payment/return`
    - Request: PG사에서 POST로 전송하는 인증 응답 데이터
        
        Content-Type: `application/x-www-form-urlencoded` (이니시스, 나이스 공통)
        
        Body: PG사별 인증 응답 필드
        
    - Response: HTML 페이지 (postMessage 스크립트 포함)
    - 프로세스
        
        ```json
        1. PG 응답 데이터 파싱 (FormData)
        2. 데이터 필드로 PG사 판별
          - 이니시스: `resultCode` 필드 존재
          - 나이스: `AuthResultCode` 필드 존재
        3. PG사별 성공 코드 확인
          - 이니시스: `resultCode === '0000'`
          - 나이스: `AuthResultCode === '0000'`
        4. 결제 결과 메시지 생성 (성공/실패, 인증 데이터, 에러 상세 정보)
        5. HTML 응답 반환 (postMessage 스크립트로 부모 창에 결과 전송)
        6. 팝업 창 자동 닫기 (1초 후)
        ```
        

### 결제 승인 요청 및 응답

승인 요청을 주문에서 사용할 수 있도록 service 메서드로 제공

Request: PaymentConfirmRequest

| 타입 | 필드명 | 구분 |
| --- | --- | --- |
| String | pgTypeCode | 공통 |
| String | authToken | 공통 |
| String | orderNo | 공통 |
| String | authUrl | 공통 |
| String | netCancelUrl | 공통 |
| String | transactionId | 나이스 |
| String | amount | 나이스 |
| String | tradeNo | 나이스 |
| String | mid | 나이스 |
| Long | price | 이니시스 |
- 나이스
    - 승인 요청
        - POST 결제 요청 응답으로 넘어온 {authUrl}
        - Request
            
            | TID | 거래번호 (인증 응답 TxTid 사용) |  |
            | --- | --- | --- |
            | AuthToken | 인증 TOKEN |  |
            | MID | 가맹점아이디 |  |
            | Amt | 금액 |  |
            | EdiDate | 전문생성일시 (YYYYMMDDHHMMSS) |  |
            | SignData | hex(sha256(AuthToken + MID + Amt + EdiDate + MerchantKey)) |  |
            | CharSet |  | `UTF-8`  고정 |
            | EdiType |  | `JSON` 고정 |
        - Response
            
            | ResultCode | 결과코드 | 3001 : 신용카드 성공 |
            | --- | --- | --- |
            | ResultMsg | 결과메시지 |  |
            | Amt | 금액 |  |
            | MID | 가맹점 ID  |  |
            | Moid | 가맹점 주문번호 |  |
            | Signature | hex(sha256(TID + MID + Amt + MerchantKey)), 위변조 검증 데이터응답 데이터 유효성 검증을 위해 가맹점 수준에서 비교하는 로직 구현을 권고합니다 |  |
            | TID | 거래ID |  |
            | AuthCode | 승인 번호 |  |
            | CardCode | 결제 카드사 코드 |  |
            | CardNo | 카드번호 |  |
- 이니시스
    - 승인 요청
        - POST 결제 요청 응답으로 넘어온 {authUrl}
        - Request
            
            | **mid** | 상점아이디 |  |
            | --- | --- | --- |
            | **authToken** | 승인요청 검증 토큰 |  |
            | **timestamp** | TimeInMillis(Long형) |  |
            | **signature** | SHA256 Hash값 | authToken={authToken}&timestamp={timestamp} |
            | **verification** | SHA256 Hash값 | authToken={authToken}&signKey={signKey}&timestamp={timestamp} |
            | **charset** |  | `UTF-8` 고정 |
            | **format** |  | `JSON` 고정 |
            | **price** | 가격 |  |
        - Response
            
            | **resultCode** | 결과코드 | 0000 성공 |
            | --- | --- | --- |
            | **resultMsg** | 결과메세지 |  |
            | **tid** | 거래번호 |  |
            | **mid** | 상점아이디 |  |
            | **MOID** | 주문번호 |  |
            | **TotPrice** | 결제금액 |  |
            | **goodName** | 상품명 |  |
            | **payMethod** | 지불수단 |  |
            | **applDate** | 승인일자 [YYYYMMDD] |  |
            | **applTime** | 승인시간 [hh24miss] |  |
            | **applNum** | 승인번호 |  |
            | **CARD_Num** | 신용카드번호 |  |
            | **CARD_Code** | 카드사 코드 |  |

### API 정의서

- 주문 번호 조회
    - GET `/api/order/generateOrderNumber`
    - Request: X
    - Response
        
        | String | orderNo | 주문번호 |
        | --- | --- | --- |
    - 프로세스
        
        시퀀스 `SEQ_ORDER_NO` 를 조회 해서 반환.
        
        이 값을 기반으로 주문, 결제, 포인트, 로그 테이블을 쌓기 때문에 최초에 조회 함.
        
- 결제 요청 정보 생성
    - POST `/api/payments/initiate`
    - Request: `PaymentInitiateRequest`
        
        | 타입 | 변수명 | 설명 |
        | --- | --- | --- |
        | Long | amount | 금액 |
        | String | goodsName | 상품명 |
        | String | memberName | 회원명 |
        | String | phoneNumber | 전화번호 |
        | String | email | 이메일 |
        | String | orderNo | 주문번호 |
    - Response: `PaymentInitiateResponse`
        
        | 타입 | 변수명 | 설명 | 필수여부 | 기타 | Request 와 매핑 될 정보 |
        | --- | --- | --- | --- | --- | --- |
        | String | pgTypeCode | PG사 코드(PAY005) | 필수 | 공통 |  |
        | String | mid | 가맹점 ID  | 필수 | 공통 | 설정파일 관리 |
        | String | goodName | 상품명 | 필수 | 공통 | goodsName |
        | String | buyerName | 회원명 | 필수 | 공통 | memberName |
        | String | buyerTel | 전화번호 | 필수 | 공통 | phoneNumber |
        | String | buyerEmail | 이메일 | 필수 | 공통 | email |
        | String | returnUrl | 결제 완료 후 리다이렉트 URL | 필수 | 공통 | 설정파일 관리 |
        | String | version | API 버전 ("1.0") | 필수 | 공통 | 설정파일 관리 |
        | String | currency | 통화 단위 ("WON") | 필수 | 공통 | 설정파일 관리 |
        | String | oid | 주문번호 | 필수 | 이니시스 | orderNo |
        | Long | price | 결제 금액 | 필수 | 이니시스 | amount |
        | String | timestamp | 요청 시각 (YYYYMMDDHHMMSS 형식) | 필수 | 이니시스 |  |
        | String | mKey | MKey (signKey SHA-256 해싱) | 필수 | 이니시스 |  |
        | String | signature | 서명 데이터 (oid, price, timestamp SHA-256 해싱) | 필수 | 이니시스 |  |
        | String | verification | 검증 데이터 (oid, price, signKey, timestamp SHA-256 해싱) | 필수 | 이니시스 |  |
        | String | closeUrl | 결제창 닫기 URL | 필수 | 이니시스 | 설정파일관리 |
        | String | gopaymethod | 결제 수단 ("Card" 등) | 선택 | 이니시스 | 설정파일관리 |
        | String | acceptmethod | 결제 허용 방식 ("below1000" 등) | 선택 | 이니시스 | 설정파일관리 |
        | String | moid | 주문 ID (가맹점 고유 번호) | 필수 | 나이스 | orderNo |
        | Long | amt | 결제 금액 | 필수 | 나이스 | amount |
        | String | ediDate | 전문 생성 일시 (YYYYMMDDHHMMSS 형식) | 필수 | 나이스 |  |
        | String | signData | 서명 데이터 (EdiDate + MID + Amt + MerchantKey 해싱) | 필수 | 나이스 |  |
        | String | cancelUrl | 결제 취소 시 리다이렉트 URL | 필수 | 나이스 | 설정파일관리 |
    - 프로세스
        
        enum 으로 관리되는 PAY005 의 `referenceValue1`으로 관리 되는 
        
        PG 가중치의 값에 따라 PG 사 선택
        
        (ex. 이니시스 10, 나이스 90 일 경우 10% 확률로 이니시스, 90%확률로 나이스 PG 선택)
        
        가중치의 합은 항상 100으로 가정하고, 이에 대한 예외는 고려하지 않을 것.
        
        선택된 PG 사에 따라 각각 요청 정보 생성(PG 사별 전략 패턴 구현)
        
        - 해싱 방식
            
            ```markdown
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, md.digest()));
            ```
            
        - 해싱 데이터

        | PG사 | 필드 | 설명 | 암호화대상데이터 |
        | --- | --- | --- | --- |
        | 이니시스 | mKey | MKey (signKey SHA-256 해싱) | {signKey} |
        | 이니시스 | signature (결제 요청) | 서명 데이터 | oid={orderNo}&price={price}&timestamp={timestamp} |
        | 이니시스 | verification (결제 요청) | 검증 데이터 | oid={orderNo}&price={price}&signKey={signKey}&timestamp={timestamp} |
        | 이니시스 | signature (승인 요청) | 서명 데이터 | authToken={authToken}&timestamp={timestamp} |
        | 이니시스 | verification (승인 요청) | 검증 데이터 | authToken={authToken}&signKey={signKey}&timestamp={timestamp} |
        | 나이스 | SignData | 서명 데이터 (EdiDate + MID + Amt + MerchantKey 해싱) | {EdiDate}{MID}{Amt}{MerchantKey} |
    
- 주문
    
    POST `/api/order/order`
    
    - Request: OrderRequest
        
        ```json
        {
        	memberNo // api에서 현재 토큰에서 꺼내서 세팅
            orderNo
        	memberName
        	phone
        	email
        	goodsList: List<BasketResponse>
        	payList: List<PayRequest>
        }
        
        ### PayRequest
        {
        	payWayCode
        	amount
        	payTypeCode
        	PaymentConfirmRequest
        }
        ```
        
    - Response
        
        응답값 없음. 주문 실패 시 예외 발생.
        
    - 프로세스
    
    ```json
    1. request 정보 기반으로 Entity 생성.
    	order_base, order_detail, order_goods
    
    2. 검증(Entity 기반)
        - 재고 검증: 주문 수량 <= 재고 수량
        - 가격 검증: 요청된 가격 == DB 가격 (가격 조작 방지) 주문시점의 가격과 비교(goods_price_hist 테이블의 salePrice 와 goods_item 테이블의 itemPrice 의 합을 요청의 salePrice 와 비교
        - 회원 검증: 정상 회원인지 회원 상태 확인 (member_status_code(MEM001) 가 001 인 회원만)
        - 장바구니 검증: 장바구니 상품이 여전히 유효한지(해당 장바구니 번호가 basket_base 테이블의 is_order 가 false 인지)
        
    3. 결제 승인
    	 한번에 포인트와 카드 복합결제가 가능하기때문에 for 문 돌면서 결제 처리.
       payList 에 payWayCode(PAY002) 에 따라 신용카드 결제 포인트 결제 진행.(전략 패턴 활용)
        001: 신용카드 결제
    	    pgTypeCode 에 맞는 PG 사에 따라 승인 처리 진행 (전략 패턴 활용)
    	    pay_base, pay_interface_log insert
    	    pay_interface_log 에는 호출했을때 사용한 요청과, 돌아온 응답 자체를 json 으로 저장한다.
        002: 포인트 결제
    			포인트 사용 API 호출(/api/point/transaction)
    			pay_base insert 
    		결제 실패 시 주문 실패 에러 발생 시키고,
    		결제가 완료 된 후에 주문이 실패할 경우 망취소 처리를 진행함.
    
    4. Entity Insert
    
    5. 후처리
    	장바구니 주문 완료 여부 갱신    
    ```
    
    - 주문서에서 결제를 진행하고, 실제 주문 처리를 하는 주문 API 에서의 값이 달라질 수 있기에, 검증 로직이 존재하고, 검증에 실패할 경우 실패 사유에 대한 예외를 던지고, 화면에서 이를 받아 처리한다.
    - 복합 결제 시 높은 우선순위의 결제는 성공했으나, 후순위 결제 실패시, 높은 우선순위의 망취소 메서드를 호출한다.
    - 결제 승인이 완료 된 후에 에러가 발생한다면, 결제에서 제공하는 망취소 메서드를 호출한다.(결제에서 제공하는 망취소 메서드의 실제 구현은 TODO 로 호출만한다.
    - 망취소는 카드 결제에 대해서는 PG 사를 재호출하고, 
    포인트 결제에 대해서는 예외 발생해 DB 가 롤백이 되기에 따로 처리하지 않는다.
    - 망취소 실패에 대해서는 고려하지 않는다.
    - 복합 결제 시 결제 및 취소 우선순위는 결제방식코드(payWayCode)`PAY002` 의 `displaySequence` 기준으로 한다. (현재. 카드, 포인트 순서)
- 주문 취소
    - POST `/api/claim/cancel`
    - Request: CancelRequest
        
        ```json
        {
        	memberNo
        	memberName
        	phone
        	email
        	goodsList: List<BasketResponse>
        	payList: List<PayRequest>
        }
        ```
        
    - Response
        - 응답값 없음. 주문 취소 실패 시 예외 발생.
    - 프로세스

### 주문/취소 DFD

예시를 위한 데이터

- goods_base
    
    | goods_no | goods_name | goods_status_code | goods_main_image_url | regist_id | regist_date_time | modify_id | modify_date_time |  |  |  |
    | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
    | G00000000000001 | 상품A | 001 | https://cdn.ftoday.co.kr/news/photo/201510/44298_46389_2338.jpg | 999999999999999 | 2025-10-29 10:52:53.590733 | 999999999999999 | 2025-10-29 10:52:53.590733 |  |  |  |
    | G00000000000021 | 상품B | 001 | https://www.meconomynews.com/news/photo/202209/70781_88611_1157.jpg | 999999999999999 | 2025-10-30 15:45:19.382214 | 999999999999999 | 2025-10-30 15:45:19.382214 |  |  |  |

---

- goods_item
    
    | goods_no | item_no | item_name | item_price | stock | goods_status_code | regist_id | regist_date_time | modify_id | modify_date_time |  |
    | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
    | G00000000000001 | 001 | 단품A | 0 | 5 | 001 | 999999999999999 | 2025-10-29 10:52:53.595171 | 999999999999999 | 2025-10-29 10:52:53.595171 |  |
    | G00000000000021 | 001 | 단품1 | 3000 | 5 | 001 | 999999999999999 | 2025-10-30 15:45:19.388662 | 999999999999999 | 2025-10-30 15:45:19.388662 |  |
    | G00000000000021 | 002 | 단품2 판매중단 | 0 | 5 | 002 | 999999999999999 | 2025-10-30 15:45:19.389562 | 999999999999999 | 2025-10-30 15:45:19.389562 |  |

---

- goods_price_hist
    
    | goods_no | start_date_time | end_date_time | sale_price | supply_price | regist_id | regist_date_time | modify_id | modify_date_time |  |  |
    | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
    | G00000000000001 | 2025-10-29 10:52:53.593352 | 9999-12-31 23:59:59.000000 | 10000 | 5000 | 999999999999999 | 2025-10-29 10:52:53.593635 | 999999999999999 | 2025-10-29 10:52:53.593635 |  |  |
    | G00000000000021 | 2025-10-30 15:45:19.386137 | 9999-12-31 23:59:59.000000 | 5000 | 4000 | 999999999999999 | 2025-10-30 15:45:19.386763 | 999999999999999 | 2025-10-30 15:45:19.386763 |  |  |

---

- member_base
    
    | member_no | member_name | phone | email | password | member_status_code | regist_id | regist_date_time | modify_id | modify_date_time |  |
    | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
    | 000000000000003 | 테스트 | 010-1234-5678 | [test@test.com](mailto:test@test.com) | $2a$10$0wV0TNKUYhGbtRM0DqHITOowAa64qmvvq4A2VHZsZ6NIZ8H7YxjZ2 | 001 | 999999999999999 | 2025-10-29 10:40:46.423024 | 999999999999999 | 2025-10-29 10:40:46.423024 |  |

- 상품A - 단품A 1개 / 카드 10000원 결제 / 이니시스
    - 주문
        - order_base 테이블
            
            | order_no | member_no |
            | --- | --- |
            | 20251030O000001 | 000000000000003 |
        
        ---
        
        - order_detail 테이블
            
            | order_no | order_sequence | order_process_sequence | upper_order_process_sequence | claim_no | goods_no | item_no | quantity | order_status_code | delivery_type_code | order_type_code | order_accept_dtm | order_finish_dtm |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | 1 | 1 |  |  | G00000000000001 | 001 | 1 | 001 | 001 | 001 | now() |  |
        
        ---
        
        - order_goods 테이블
            
            | order_no | goods_no | item_no | sale_price | supply_price | goods_name | item_name |
            | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | G00000000000001 | 001 | 10000 | 5000 | 상품A | 단품A |
        - pay_base 테이블
            
            | pay_no | pay_type_code | pay_way_code | pay_status_code | approve_no | order_no | claim_no | upper_pay_no | trd_no | pay_finish_date_time | member_no | amount | cancelable_amount | pg_type_code |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 000000000000002 | 001 | 001 | 002 |  | 20251030O000001 |  |  |  | now() | 000000000000003 | 10000 | 10000 | 001 |
        
        ---
        
        - pay_interface_log 테이블
            
            | pay_interface_no | member_no | pay_no | pay_log_code | request_json | response_json |
            | --- | --- | --- | --- | --- | --- |
            | 000000000000001 | 000000000000003 | 000000000000002 | 001 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            | 000000000000002 | 000000000000003 | 000000000000002 | 002 | 실제 요청 json 정보 | 실제 응답 json 정보 |
        
        - point_history 테이블
            
            | point_history_no | member_no | amount | pointTransactionCode | point_transaction_reson_code | point_transaction_reson_no | start_date_time | end_date_time | upper_point_history_no | remain_point |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            |  |  |  |  |  |  |  |  |  |  |
        
    - 주문 취소
        - order_base 테이블
            
            | order_no | member_no |
            | --- | --- |
            | 20251030O000001 | 000000000000003 |
        
        ---
        
        - order_detail 테이블
            
            | order_no | order_sequence | order_process_sequence | upper_order_process_sequence | claim_no | goods_no | item_no | quantity | order_status_code | delivery_type_code | order_type_code | order_accept_dtm | order_finish_dtm |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | 1 | 1 |  |  | G00000000000001 | 001 | 1 | 001 | 001 | 001 | now() |  |
            | 20251030O000001 | 1 | 2 | 1 | 20251030C000001 | G00000000000001 | 001 | 1 | 007 | 002 | 002 | now() |  |
        
        ---
        
        - order_goods 테이블
            
            | order_no | goods_no | item_no | sale_price | supply_price | goods_name | item_name |
            | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | G00000000000001 | 001 | 10000 | 5000 | 상품A | 단품A |
        - pay_base 테이블
            
            | pay_no | pay_type_code | pay_way_code | pay_status_code | approve_no | order_no | claim_no | upper_pay_no | trd_no | pay_finish_date_time | member_no | amount | cancelable_amount | pg_type_code |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 000000000000002 | 001 | 001 | 002 |  | 20251030O000001 |  |  | PG사에서 넘어온 transactionId | now() | 000000000000003 | 10000 | 0 | 001 |
            | 000000000000003 | 002 | 001 | 003 |  | 20251030O000001 | 20251030C000001 | 000000000000002 | PG사에서 넘어온 transactionId | now() | 000000000000003 | 10000 |  | 001 |
        
        ---
        
        - pay_interface_log 테이블
            
            | pay_interface_no | member_no | pay_no | pay_log_code | request_json | response_json |
            | --- | --- | --- | --- | --- | --- |
            | 000000000000001 | 000000000000003 | 000000000000002 | 001 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            | 000000000000002 | 000000000000003 | 000000000000002 | 002 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            | 000000000000003 | 000000000000003 | 000000000000003 | 004 | 실제 요청 json 정보 | 실제 응답 json 정보 |
        
        - point_history 테이블
            
            | point_history_no | member_no | amount | pointTransactionCode | point_transaction_reson_code | point_transaction_reson_no | start_date_time | end_date_time | upper_point_history_no | remain_point |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            |  |  |  |  |  |  |  |  |  |  |
    
    - 상품B - 단품1 2개 / 포인트 5000원 카드 11000원 결제 / 나이스
        - 주문
            - order_base 테이블
                
                | order_no | member_no |
                | --- | --- |
                | 20251030O000002 | 000000000000003 |
            
            ---
            
            - order_detail 테이블
                
                | order_no | order_sequence | order_process_sequence | upper_order_process_sequence | claim_no | goods_no | item_no | quantity | order_status_code | delivery_type_code | order_type_code | order_accept_dtm | order_finish_dtm |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 20251030O000002 | 1 | 1 |  |  | G00000000000021 | 001 | 2 | 001 | 001 | 001 | now() |  |
            
            ---
            
            - order_goods 테이블
                
                | order_no | goods_no | item_no | sale_price | supply_price | goods_name | item_name |
                | --- | --- | --- | --- | --- | --- | --- |
                | 20251030O000002 | G00000000000001 | 001 | goods_price_hist 의 sale_price + goods_item 의 item_price
                5000+3000 | 4000 | 상품B | 단품1 |
            - pay_base 테이블
                
                | pay_no | pay_type_code | pay_way_code | pay_status_code | approve_no | order_no | claim_no | upper_pay_no | trd_no | pay_finish_date_time | member_no | amount | cancelable_amount | pg_type_code |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 000000000000003 | 001 | 001 | 002 |  | 20251030O000002 |  |  |  | now() | 000000000000003 | 11000 | 11000 | 002 |
                | 000000000000004 | 001 | 002 | 002 |  | 20251030O000002 |  |  |  | now() | 000000000000003 | 5000 | 5000 |  |
            
            ---
            
            - pay_interface_log 테이블
                
                | pay_interface_no | member_no | pay_no | pay_log_code | request_json | response_json |
                | --- | --- | --- | --- | --- | --- |
                | 000000000000003 | 000000000000003 | 000000000000003 | 001 | 실제 요청 json 정보 | 실제 응답 json 정보 |
                | 000000000000004 | 000000000000003 | 000000000000003 | 002 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            
            - point_history 테이블
                
                | point_history_no | member_no | amount | pointTransactionCode | point_transaction_reson_code | point_transaction_reson_no | start_date_time | end_date_time | upper_point_history_no | remain_point |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 000000000000001 | 000000000000003 | 5000 | 001 | 002 | 000000000000004 (pay_no) | now() | now() + `MEM003` 의 `referenceValue1` |  | 5000 |
        - 주문 취소
            - order_base 테이블
                
                | order_no | member_no |
                | --- | --- |
                | 20251030O000002 | 000000000000003 |
            
            ---
            
            - order_detail 테이블
                
                | order_no | order_sequence | order_process_sequence | upper_order_process_sequence | claim_no | goods_no | item_no | quantity | order_status_code | delivery_type_code | order_type_code | order_accept_dtm | order_finish_dtm |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 20251030O000002 | 1 | 1 |  |  | G00000000000021 | 001 | 2 | 001 | 001 | 001 | now() |  |
                | 20251030O000002 | 1 | 2 | 1 | 20251030C000002 | G00000000000021 | 001 | 2 | 007 | 002 | 002 | now() |  |
            
            ---
            
            - order_goods 테이블
                
                | order_no | goods_no | item_no | sale_price | supply_price | goods_name | item_name |
                | --- | --- | --- | --- | --- | --- | --- |
                | 20251030O000002 | G00000000000001 | 001 | goods_price_hist 의 sale_price + goods_item 의 item_price
                5000+3000 | 4000 | 상품B | 단품1 |
            - pay_base 테이블
                
                | pay_no | pay_type_code | pay_way_code | pay_status_code | approve_no | order_no | claim_no | upper_pay_no | trd_no | pay_finish_date_time | member_no | amount | cancelable_amount | pg_type_code |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 000000000000003 | 001 | 001 | 002 |  | 20251030O000002 |  |  | PG사에서 넘어온 transactionId | 결제시점 | 000000000000003 | 11000 | 0 | 002 |
                | 000000000000004 | 001 | 002 | 002 |  | 20251030O000002 |  |  | PG사에서 넘어온 transactionId | 결제시점 | 000000000000003 | 5000 | 0 |  |
                | 000000000000005 | 002 | 001 | 003 |  | 20251030O000002 | 20251030C000002 | 000000000000003 | PG사에서 넘어온 transactionId | now() | 000000000000003 | 11000 |  | 002 |
                | 000000000000006 | 002 | 002 | 003 |  | 20251030O000002 | 20251030C000002 | 000000000000004 | PG사에서 넘어온 transactionId | now() | 000000000000003 | 5000 |  |  |
            
            ---
            
            - pay_interface_log 테이블
                
                | pay_interface_no | member_no | pay_no | pay_log_code | request_json | response_json |
                | --- | --- | --- | --- | --- | --- |
                | 000000000000003 | 000000000000003 | 000000000000003 | 001 | 실제 요청 json 정보 | 실제 응답 json 정보 |
                | 000000000000004 | 000000000000003 | 000000000000003 | 002 | 실제 요청 json 정보 | 실제 응답 json 정보 |
                | 000000000000005 | 000000000000003 | 000000000000005 | 004 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            
            - point_history 테이블
                
                | point_history_no | member_no | amount | pointTransactionCode | point_transaction_reson_code | point_transaction_reson_no | start_date_time | end_date_time | upper_point_history_no | remain_point |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 000000000000001 | 000000000000003 | 5000 | 001 | 002 | 000000000000004 (pay_no) | now() | now() + `MEM003` 의 `referenceValue1` |  | 0 |
                | 000000000000002 | 000000000000003 | 5000 | 002 | 002 | 000000000000006 (pay_no) | now() | now() + `MEM003` 의 `referenceValue1` | 000000000000001 | 5000 |
    
    - 상품 N 개 주문
        - 카드 단독
            
            상품A - 단품A 1개
            
            상품B - 단품1 1개 
            
            카드 18000원 / 이니시스
            
            - order_base 테이블
                
                | order_no | member_no |
                | --- | --- |
                | 20251030O000001 | 000000000000003 |
            
            ---
            
            - order_detail 테이블
                
                | order_no | order_sequence | order_process_sequence | upper_order_process_sequence | claim_no | goods_no | item_no | quantity | order_status_code | delivery_type_code | order_type_code | order_accept_dtm | order_finish_dtm |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 20251030O000001 | 1 | 1 |  |  | G00000000000001 | 001 | 1 | 001 | 001 | 001 | now() |  |
                | 20251030O000001 | 2 | 1 |  |  | G00000000000021 | 001 | 1 | 001 | 001 | 001 | now() |  |
            
            ---
            
            - order_goods 테이블
                
                | order_no | goods_no | item_no | sale_price | supply_price | goods_name | item_name |
                | --- | --- | --- | --- | --- | --- | --- |
                | 20251030O000001 | G00000000000001 | 001 | 10000 | 5000 | 상품A | 단품A |
                | 20251030O000001 | G00000000000001 | 001 | 8000 | 4000 | 상품B | 단품1 |
            - pay_base 테이블
                
                | pay_no | pay_type_code | pay_way_code | pay_status_code | approve_no | order_no | claim_no | upper_pay_no | trd_no | pay_finish_date_time | member_no | amount | cancelable_amount | pg_type_code |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                | 000000000000002 | 001 | 001 | 002 |  | 20251030O000001 |  |  |  | now() | 000000000000003 | 18000 | 18000 | 001 |
            
            ---
            
            - pay_interface_log 테이블
                
                | pay_interface_no | member_no | pay_no | pay_log_code | request_json | response_json |
                | --- | --- | --- | --- | --- | --- |
                | 000000000000001 | 000000000000003 | 000000000000002 | 001 | 실제 요청 json 정보 | 실제 응답 json 정보 |
                | 000000000000002 | 000000000000003 | 000000000000002 | 002 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            
            - point_history 테이블
                
                | point_history_no | member_no | amount | pointTransactionCode | point_transaction_reson_code | point_transaction_reson_no | start_date_time | end_date_time | upper_point_history_no | remain_point |
                | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
                |  |  |  |  |  |  |  |  |  |  |
- 주문취소
    - 카드, 포인트로 상품 1개 주문 후 전체 취소
        - order_base 테이블
            
            | order_no | member_no |
            | --- | --- |
            | 20251030O000001 | 000000000000003 |
        
        ---
        
        - order_detail 테이블
            
            | order_no | order_sequence | order_process_sequence | upper_order_process_sequence | claim_no | goods_no | item_no | quantity | order_status_code | delivery_type_code | order_type_code | order_accept_dtm | order_finish_dtm |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | 1 | 1 |  |  | G00000000000001 | 001 | 1 | 001 | 001 | 001 | 주문시점 |  |
            | 20251030O000001 | 1 | 2 | 1 | 20251030C000001 | G00000000000001 | 001 | 1 |  | 002 | 003 | now() |  |
        
        ---
        
        - order_goods 테이블
            
            | order_no | goods_no | item_no | sale_price | supply_price | goods_name | item_name |
            | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | G00000000000001 | 001 | 10000 | 5000 | 상품A | 단품A |
        - pay_base 테이블
            
            | pay_no | pay_type_code | pay_way_code | pay_status_code | approve_no | order_no | claim_no | upper_pay_no | trd_no | pay_finish_date_time | member_no | amount | cancelable_amount | pg_type_code |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 000000000000002 | 001 | 001 | 002 |  | 20251030O000001 |  |  |  | now() | 000000000000003 | 10000 | 10000 | 001 |
        
        ---
        
        - pay_interface_log 테이블
            
            | pay_interface_no | member_no | pay_no | pay_log_code | request_json | response_json |
            | --- | --- | --- | --- | --- | --- |
            | 000000000000001 | 000000000000003 | 000000000000002 | 001 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            | 000000000000002 | 000000000000003 | 000000000000002 | 002 | 실제 요청 json 정보 | 실제 응답 json 정보 |
        
        - point_history 테이블
            
            | point_history_no | member_no | amount | pointTransactionCode | point_transaction_reson_code | point_transaction_reson_no | start_date_time | end_date_time | upper_point_history_no | remain_point |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            |  |  |  |  |  |  |  |  |  |  |
    
    - 카드, 포인트로 상품 N 개 주문 후 상품 부분 취소
        
        상품A - 단품A 1개 10,000원
        
        상품B - 단품1 1개 8,000원
        
        카드 6,000원 / 이니시스
        
        포인트 12,000원 
        
        포인트 
        
        - order_base 테이블
            
            | order_no | member_no |
            | --- | --- |
            | 20251030O000001 | 000000000000003 |
        
        ---
        
        - order_detail 테이블
            
            | order_no | order_sequence | order_process_sequence | upper_order_process_sequence | claim_no | goods_no | item_no | quantity | order_status_code | delivery_type_code | order_type_code | order_accept_dtm | order_finish_dtm |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | 1 | 1 |  |  | G00000000000001 | 001 | 1 | 001 | 001 | 001 | now() |  |
            | 20251030O000001 | 2 | 1 |  |  | G00000000000021 | 001 | 1 | 001 | 001 | 001 | now() |  |
        
        ---
        
        - order_goods 테이블
            
            | order_no | goods_no | item_no | sale_price | supply_price | goods_name | item_name |
            | --- | --- | --- | --- | --- | --- | --- |
            | 20251030O000001 | G00000000000001 | 001 | 10000 | 5000 | 상품A | 단품A |
            | 20251030O000001 | G00000000000001 | 001 | 8000 | 4000 | 상품B | 단품1 |
        - pay_base 테이블
            
            | pay_no | pay_type_code | pay_way_code | pay_status_code | approve_no | order_no | claim_no | upper_pay_no | trd_no | pay_finish_date_time | member_no | amount | cancelable_amount | pg_type_code |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            | 000000000000002 | 001 | 001 | 002 |  | 20251030O000001 |  |  |  | now() | 000000000000003 | 18000 | 18000 | 001 |
        
        ---
        
        - pay_interface_log 테이블
            
            | pay_interface_no | member_no | pay_no | pay_log_code | request_json | response_json |
            | --- | --- | --- | --- | --- | --- |
            | 000000000000001 | 000000000000003 | 000000000000002 | 001 | 실제 요청 json 정보 | 실제 응답 json 정보 |
            | 000000000000002 | 000000000000003 | 000000000000002 | 002 | 실제 요청 json 정보 | 실제 응답 json 정보 |
        
        - point_history 테이블
            
            | point_history_no | member_no | amount | pointTransactionCode | point_transaction_reson_code | point_transaction_reson_no | start_date_time | end_date_time | upper_point_history_no | remain_point |
            | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
            |  |  |  |  |  |  |  |  |  |  |
        
    - 카드로 상품 N개 주문 후 상품 부분 취소 후 전체취소