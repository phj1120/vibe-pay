# 2. 기능 요구사항 (Functional Requirements)

## 2.1. 회원 관리

### 2.1.1. 회원 생성
- **목적**: 새로운 사용자를 시스템에 등록합니다.
- **입력**: `Member` 객체 (name, shippingAddress, phoneNumber, email)
- **출력**: 생성된 `Member` 객체 (memberId 포함)
- **비즈니스 규칙**:
    - `name`, `shippingAddress`, `phoneNumber`, `email`은 필수 값입니다.
    - `email`과 `phoneNumber`는 시스템 내에서 고유해야 합니다.
    - 회원 생성 시 `createdAt`은 현재 시각으로 자동 설정됩니다.
    - 회원 생성과 동시에 해당 회원의 `RewardPoints` 레코드가 0 포인트로 생성됩니다.
- **사용자 흐름**:
    1. 사용자가 회원 가입 정보를 입력합니다.
    2. 시스템은 입력된 정보를 검증합니다.
    3. 유효한 정보인 경우, `Member` 레코드와 `RewardPoints` 레코드를 생성하고 회원 ID를 반환합니다.
- **성공 조건**: 유효한 회원 정보로 `Member` 및 `RewardPoints` 레코드 생성 후 200 OK와 생성된 회원 정보 반환.
- **실패 조건**: 필수 값 누락, 중복된 `email` 또는 `phoneNumber`, 기타 유효성 검증 실패 시 400 Bad Request.

### 2.1.2. 회원 조회 (단건)
- **목적**: 특정 회원의 상세 정보를 조회합니다.
- **입력**: `memberId` (Long)
- **출력**: `Member` 객체
- **비즈니스 규칙**: `memberId`에 해당하는 회원이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `memberId`를 전달하여 회원 정보 조회를 요청합니다.
    2. 시스템은 해당 `memberId`의 회원 정보를 찾아 반환합니다.
- **성공 조건**: `memberId`에 해당하는 회원 정보 반환 시 200 OK.
- **실패 조건**: `memberId`에 해당하는 회원이 없을 경우 404 Not Found.

### 2.1.3. 회원 목록 조회
- **목적**: 시스템에 등록된 모든 회원의 목록을 조회합니다.
- **입력**: 없음
- **출력**: `Member` 객체 리스트
- **비즈니스 규칙**: 없음
- **사용자 흐름**:
    1. 시스템에 회원 목록 조회를 요청합니다.
    2. 시스템은 모든 회원 정보를 반환합니다.
- **성공 조건**: 회원 목록 반환 시 200 OK.
- **실패 조건**: (현재 없음, 내부 서버 오류 시 500 Internal Server Error)

### 2.1.4. 회원 정보 수정
- **목적**: 특정 회원의 정보를 업데이트합니다.
- **입력**: `memberId` (Long), `Member` 객체 (수정할 필드)
- **출력**: 수정된 `Member` 객체
- **비즈니스 규칙**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `email`과 `phoneNumber`는 수정 시에도 고유성을 유지해야 합니다.
- **사용자 흐름**:
    1. 사용자가 `memberId`와 수정할 회원 정보를 입력합니다.
    2. 시스템은 `memberId`에 해당하는 회원을 찾아 정보를 업데이트하고 반환합니다.
- **성공 조건**: 회원 정보 수정 후 200 OK와 수정된 회원 정보 반환.
- **실패 조건**: `memberId`에 해당하는 회원이 없거나, 유효성 검증 실패 시 404 Not Found 또는 400 Bad Request.

### 2.1.5. 회원 삭제
- **목적**: 특정 회원을 시스템에서 제거합니다.
- **입력**: `memberId` (Long)
- **출력**: 없음
- **비즈니스 규칙**: `memberId`에 해당하는 회원이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `memberId`를 전달하여 회원 삭제를 요청합니다.
    2. 시스템은 해당 `memberId`의 회원 정보를 삭제합니다.
- **성공 조건**: 회원 삭제 후 204 No Content.
- **실패 조건**: `memberId`에 해당하는 회원이 없을 경우 404 Not Found.

## 2.2. 상품 관리

### 2.2.1. 상품 생성
- **목적**: 새로운 상품을 시스템에 등록합니다.
- **입력**: `Product` 객체 (name, price)
- **출력**: 생성된 `Product` 객체 (productId 포함)
- **비즈니스 규칙**:
    - `name`, `price`는 필수 값입니다.
    - `name`은 고유해야 합니다.
    - `price`는 0보다 커야 합니다.
- **사용자 흐름**:
    1. 관리자가 상품 정보를 입력합니다.
    2. 시스템은 입력된 정보를 검증합니다.
    3. 유효한 정보인 경우, `Product` 레코드를 생성하고 상품 ID를 반환합니다.
- **성공 조건**: 유효한 상품 정보로 `Product` 레코드 생성 후 200 OK와 생성된 상품 정보 반환.
- **실패 조건**: 필수 값 누락, 중복된 `name`, `price`가 0 이하인 경우 등 유효성 검증 실패 시 400 Bad Request.

### 2.2.2. 상품 조회 (단건)
- **목적**: 특정 상품의 상세 정보를 조회합니다.
- **입력**: `productId` (Long)
- **출력**: `Product` 객체
- **비즈니스 규칙**: `productId`에 해당하는 상품이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `productId`를 전달하여 상품 정보 조회를 요청합니다.
    2. 시스템은 해당 `productId`의 상품 정보를 찾아 반환합니다.
- **성공 조건**: `productId`에 해당하는 상품 정보 반환 시 200 OK.
- **실패 조건**: `productId`에 해당하는 상품이 없을 경우 404 Not Found.

### 2.2.3. 상품 목록 조회
- **목적**: 시스템에 등록된 모든 상품의 목록을 조회합니다.
- **입력**: 없음
- **출력**: `Product` 객체 리스트
- **비즈니스 규칙**: 없음
- **사용자 흐름**:
    1. 시스템에 상품 목록 조회를 요청합니다.
    2. 시스템은 모든 상품 정보를 반환합니다.
- **성공 조건**: 상품 목록 반환 시 200 OK.
- **실패 조건**: (현재 없음, 내부 서버 오류 시 500 Internal Server Error)

### 2.2.4. 상품 정보 수정
- **목적**: 특정 상품의 정보를 업데이트합니다.
- **입력**: `productId` (Long), `Product` 객체 (수정할 필드)
- **출력**: 수정된 `Product` 객체
- **비즈니스 규칙**:
    - `productId`에 해당하는 상품이 존재해야 합니다.
    - `name`은 수정 시에도 고유성을 유지해야 합니다.
    - `price`는 0보다 커야 합니다.
- **사용자 흐름**:
    1. 관리자가 `productId`와 수정할 상품 정보를 입력합니다.
    2. 시스템은 `productId`에 해당하는 상품을 찾아 정보를 업데이트하고 반환합니다.
- **성공 조건**: 상품 정보 수정 후 200 OK와 수정된 상품 정보 반환.
- **실패 조건**: `productId`에 해당하는 상품이 없거나, 유효성 검증 실패 시 404 Not Found 또는 400 Bad Request.

### 2.2.5. 상품 삭제
- **목적**: 특정 상품을 시스템에서 제거합니다.
- **입력**: `productId` (Long)
- **출력**: 없음
- **비즈니스 규칙**:
    - `productId`에 해당하는 상품이 존재해야 합니다.
    - 이미 주문에 포함된 상품은 삭제할 수 없습니다 (참조 무결성).
- **사용자 흐름**:
    1. 시스템에 `productId`를 전달하여 상품 삭제를 요청합니다.
    2. 시스템은 해당 `productId`의 상품 정보를 삭제합니다.
- **성공 조건**: 상품 삭제 후 204 No Content.
- **실패 조건**: `productId`에 해당하는 상품이 없거나, 참조 무결성 위반 시 404 Not Found 또는 400 Bad Request.

## 2.3. 주문 처리

### 2.3.1. 주문 번호 채번
- **목적**: 새로운 주문을 생성하기 전에 고유한 주문 번호를 발급합니다.
- **입력**: 없음
- **출력**: `orderNumber` (String, 예: `YYYYMMDDO00000001`)
- **비즈니스 규칙**:
    - 주문 번호는 `YYYYMMDDO` + 8자리 순번 형식으로 생성됩니다.
    - 순번은 매일 초기화되지 않고, 전체 시스템에서 유일하게 증가하는 시퀀스를 사용합니다.
- **사용자 흐름**:
    1. 클라이언트가 주문 번호 채번을 요청합니다.
    2. 시스템은 현재 날짜와 내부 시퀀스를 조합하여 고유한 주문 번호를 생성하고 반환합니다.
- **성공 조건**: 고유한 주문 번호 반환 시 200 OK.
- **실패 조건**: (내부 서버 오류 시 500 Internal Server Error)

### 2.3.2. 주문 생성 및 결제 승인
- **목적**: 사용자가 선택한 상품에 대해 결제를 진행하고 주문을 생성합니다.
- **입력**: `OrderRequest` 객체
    - `orderNumber` (String): 채번된 주문 번호
    - `memberId` (Long): 주문자 회원 ID
    - `items` (List<OrderItemRequest>): 주문 상품 목록 (productId, quantity)
    - `paymentMethods` (List<PaymentMethodRequest>): 결제 수단 목록 (authToken, authUrl, amount, paymentMethod, pgCompany 등)
    - `netCancel` (boolean): 테스트용 망취소 플래그
- **출력**: 생성된 `Order` 객체 리스트
- **비즈니스 규칙**:
    - `orderNumber`, `memberId`, `items`, `paymentMethods`는 필수 값입니다.
    - `items`에 포함된 `productId`는 유효한 상품이어야 합니다.
    - `items`의 `quantity`는 1 이상이어야 합니다.
    - `paymentMethods`의 `amount` 합계는 주문 상품의 총 금액과 일치해야 합니다.
    - 각 `paymentMethod`에 대해 해당 PG사(이니시스, 나이스페이, 토스페이먼츠)를 통해 결제 승인을 시도합니다.
    - 결제 승인 성공 시:
        - 각 주문 상품별로 `ORDERS` 및 `ORDER_ITEM` 레코드를 생성합니다. (`ord_proc_seq`는 1로 시작)
        - `PAYMENT` 레코드를 생성하여 결제 정보를 기록합니다.
        - 포인트 결제 시 `REWARD_POINTS`에서 포인트를 차감하고 `POINT_HISTORY`에 사용 내역을 기록합니다.
    - 결제 승인 실패 시:
        - `PaymentException` 발생.
        - 주문 생성 트랜잭션은 롤백됩니다.
    - 결제 승인 성공 후 주문 생성 중 오류 발생 시:
        - `OrderException` 발생.
        - 주문 생성 트랜잭션은 롤백됩니다.
        - 이미 승인된 결제에 대해 PG사를 통해 **망취소(Net Cancel)**를 시도합니다.
        - 망취소 실패 시 로그를 기록하고 관리자에게 알림을 보냅니다.
- **사용자 흐름**:
    1. 사용자가 상품을 선택하고 결제 정보를 입력합니다.
    2. 클라이언트는 주문 번호를 채번하고, 결제 시작 요청을 통해 PG사 결제창을 띄웁니다.
    3. 사용자가 PG사 결제창에서 결제를 완료하면, PG사는 클라이언트로 결제 결과를 리다이렉트합니다.
    4. 클라이언트는 결제 결과를 포함한 `OrderRequest`를 서버에 전송하여 주문 생성 및 결제 승인을 요청합니다.
    5. 서버는 각 결제 수단에 대해 PG사 승인 API를 호출하고, 성공 시 주문 및 결제 관련 데이터를 DB에 기록합니다.
    6. 모든 과정이 성공하면 주문 완료 응답을 반환합니다.
- **성공 조건**: 모든 결제 승인 및 주문/결제 데이터 기록 성공 시 200 OK와 생성된 `Order` 리스트 반환.
- **실패 조건**: 결제 승인 실패 시 400 Bad Request (Payment approval failed 메시지 포함). 주문 생성 중 오류 발생 시 500 Internal Server Error (Order creation failed after payment approval 메시지 포함) 및 망취소 시도.

### 2.3.3. 주문 조회 (단건)
- **목적**: 특정 주문 번호에 해당하는 모든 주문 처리 이력(최초 주문, 취소 등)을 조회합니다.
- **입력**: `orderId` (String)
- **출력**: `Order` 객체 리스트
- **비즈니스 규칙**: `orderId`에 해당하는 주문이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `orderId`를 전달하여 주문 조회를 요청합니다.
    2. 시스템은 해당 `orderId`의 모든 `ord_proc_seq`에 해당하는 주문 정보를 찾아 반환합니다.
- **성공 조건**: `orderId`에 해당하는 주문 정보 리스트 반환 시 200 OK.
- **실패 조건**: `orderId`에 해당하는 주문이 없을 경우 404 Not Found.

### 2.3.4. 회원별 주문 목록 조회
- **목적**: 특정 회원의 모든 주문 내역을 조회합니다.
- **입력**: `memberId` (Long)
- **출력**: `Order` 객체 리스트
- **비즈니스 규칙**: `memberId`에 해당하는 회원이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `memberId`를 전달하여 회원 주문 목록 조회를 요청합니다.
    2. 시스템은 해당 `memberId`의 모든 주문 정보를 찾아 반환합니다.
- **성공 조건**: `memberId`에 해당하는 주문 정보 리스트 반환 시 200 OK.
- **실패 조건**: (현재 없음, 내부 서버 오류 시 500 Internal Server Error)

### 2.3.5. 회원별 주문 상세 조회 (페이징 포함)
- **목적**: 특정 회원의 주문 내역을 상세하게 조회합니다. 각 주문에 대한 상품 정보, 결제 정보, 주문 처리 이력(취소 등)을 포함합니다.
- **입력**: `memberId` (Long), `page` (int, 기본값 0), `size` (int, 기본값 10)
- **출력**: `OrderDetailDto` 객체 리스트
- **비즈니스 규칙**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `page`와 `size`를 이용하여 페이징 처리합니다.
    - 각 `OrderDetailDto`는 `Order` (원본 주문), `List<OrderItemDto>`, `List<Payment>`, `List<Order>` (모든 처리 이력)를 포함합니다.
- **사용자 흐름**:
    1. 시스템에 `memberId`, `page`, `size`를 전달하여 회원 주문 상세 조회를 요청합니다.
    2. 시스템은 해당 회원의 원본 주문(`ord_proc_seq = 1`)을 페이징하여 조회합니다.
    3. 각 원본 주문에 대해 연관된 주문 항목, 결제 정보, 모든 주문 처리 이력을 취합하여 `OrderDetailDto` 형태로 반환합니다.
- **성공 조건**: `OrderDetailDto` 리스트 반환 시 200 OK.
- **실패 조건**: `memberId`에 해당하는 회원이 없거나, 내부 서버 오류 시 400 Bad Request 또는 500 Internal Server Error.

### 2.3.6. 주문 취소
- **목적**: 특정 주문을 취소하고 관련 결제를 환불 처리합니다.
- **입력**: `orderId` (String)
- **출력**: 취소된 `Order` 객체 (최초 취소건)
- **비즈니스 규칙**:
    - `orderId`에 해당하는 원본 주문(`ord_proc_seq = 1`)이 존재해야 합니다.
    - 이미 취소된 주문은 다시 취소할 수 없습니다.
    - 취소 시 새로운 `claimId`를 발급합니다.
    - 원본 주문의 각 `ord_seq`에 대해 `ord_proc_seq`를 1 증가시킨 새로운 `ORDERS` 레코드를 생성합니다. 이 레코드의 `total_amount`와 `quantity`는 음수 값을 가집니다.
    - `status`는 `CANCELLED`로 설정됩니다.
    - 해당 주문과 연관된 모든 `PAYMENT`에 대해 PG사를 통해 환불(취소)을 시도합니다.
    - 포인트 결제가 포함된 경우, `REWARD_POINTS`에 포인트를 복원하고 `POINT_HISTORY`에 환불 내역을 기록합니다.
- **사용자 흐름**:
    1. 사용자가 `orderId`를 전달하여 주문 취소를 요청합니다.
    2. 시스템은 주문의 유효성을 검증하고, 새로운 `claimId`를 생성합니다.
    3. 원본 주문에 대한 취소 주문 레코드와 취소 주문 항목 레코드를 생성합니다.
    4. 연관된 결제에 대해 PG사 환불 API를 호출하고, 포인트 복원 로직을 수행합니다.
    5. 모든 과정이 성공하면 취소된 주문 정보를 반환합니다.
- **성공 조건**: 주문 취소 및 관련 결제 환불 성공 시 200 OK와 취소된 `Order` 객체 반환.
- **실패 조건**: `orderId`에 해당하는 주문이 없거나, 이미 취소된 주문인 경우, PG사 환불 실패 시 400 Bad Request.

## 2.4. 결제 처리

### 2.4.1. 결제 시작 (Initiate Payment)
- **목적**: PG사 결제창을 띄우기 위한 초기 정보를 생성하고 반환합니다.
- **입력**: `PaymentInitiateRequest` 객체
    - `memberId` (Long)
    - `amount` (Long)
    - `paymentMethod` (String, 예: `CREDIT_CARD`, `POINT`)
    - `pgCompany` (String, 예: `INICIS`, `NICEPAY`, `TOSS`, `WEIGHTED`)
    - `goodName`, `buyerName`, `buyerTel`, `buyerEmail`, `orderId` (String)
- **출력**: `PaymentInitResponse` 객체
    - `success` (boolean)
    - `paymentUrl` (String): PG사 결제창 URL
    - `paymentParams` (String): PG사 결제창에 전달할 파라미터 (JSON 또는 HTML Form)
    - `errorMessage` (String)
    - `paymentId` (String): 시스템 내부 결제 ID
    - `selectedPgCompany` (String): 실제로 선택된 PG사 (WEIGHTED 선택 시)
- **비즈니스 규칙**:
    - `memberId`, `amount`, `paymentMethod`, `pgCompany`, `orderId`는 필수 값입니다.
    - `amount`는 0보다 커야 합니다.
    - `pgCompany`가 `WEIGHTED`인 경우, 시스템은 내부 가중치 설정에 따라 PG사를 동적으로 선택합니다.
    - 선택된 PG사에 따라 `PaymentGatewayAdapter`를 통해 PG사 초기화 API를 호출합니다.
    - PG사 초기화 응답을 받아 `PaymentInitResponse` 형태로 가공하여 반환합니다.
    - 모든 PG사와의 통신 내역은 `PAYMENT_INTERFACE_REQUEST_LOG`에 기록됩니다.
- **사용자 흐름**:
    1. 클라이언트가 `PaymentInitiateRequest`를 서버에 전송하여 결제 시작을 요청합니다.
    2. 서버는 요청을 검증하고, PG사를 선택(가중치 기반 포함)하여 해당 PG사의 결제 초기화 API를 호출합니다.
    3. PG사로부터 받은 응답을 클라이언트가 결제창을 띄울 수 있는 형태로 가공하여 반환합니다.
- **성공 조건**: PG사 결제 초기화 성공 시 200 OK와 `success: true`를 포함한 `PaymentInitResponse` 반환.
- **실패 조건**: 필수 값 누락, 유효성 검증 실패, PG사 초기화 실패 시 400 Bad Request와 `success: false`, `errorMessage` 포함한 `PaymentInitResponse` 반환.

### 2.4.2. 결제 승인 (Confirm Payment)
- **목적**: PG사로부터 전달받은 결제 결과를 최종 승인하고, 시스템에 결제 정보를 기록합니다.
- **입력**: `PaymentConfirmRequest` 객체 (PG사로부터 받은 파라미터 포함)
    - `authToken`, `authUrl`, `netCancelUrl`, `mid`, `orderId`, `price` (PG사 응답 파라미터)
    - `memberId`, `paymentId`, `paymentMethod`, `pgCompany`, `txTid`, `nextAppUrl` (내부 정보)
- **출력**: `PaymentReturnResponse` 객체
    - `success` (boolean)
    - `message` (String)
    - `resultCode` (String)
    - `payment` (Payment 객체, 성공 시)
- **비즈니스 규칙**:
    - `resultCode`가 `0000` (성공)인 경우에만 결제 승인 로직을 진행합니다.
    - `paymentMethod`에 따라 적절한 `PaymentProcessor`를 선택하여 결제를 처리합니다.
    - 신용카드 결제 시:
        - PG사 승인 API를 호출하여 최종 승인을 요청합니다.
        - 승인 성공 시 `PAYMENT` 레코드를 생성하고 `status`를 `SUCCESS`로 설정합니다.
        - 승인 실패 시 `PAYMENT` 레코드를 `FAILED` 상태로 기록하고 `PaymentException`을 발생시킵니다.
    - 포인트 결제 시:
        - `REWARD_POINTS`에서 포인트를 차감하고 `POINT_HISTORY`에 사용 내역을 기록합니다.
        - `PAYMENT` 레코드를 생성하고 `status`를 `SUCCESS`로 설정합니다.
        - 포인트 부족 시 `PaymentException`을 발생시키고 `PAYMENT` 레코드를 `FAILED` 상태로 기록합니다.
    - 모든 PG사와의 통신 내역은 `PAYMENT_INTERFACE_REQUEST_LOG`에 기록됩니다.
- **사용자 흐름**:
    1. PG사 결제창에서 결제 완료 후, PG사는 클라이언트의 `/api/payments/return` 엔드포인트로 결제 결과를 POST 요청합니다.
    2. 서버는 PG사로부터 받은 파라미터를 파싱하고 `resultCode`를 확인합니다.
    3. `resultCode`가 성공인 경우, `PaymentConfirmRequest`를 구성하여 `PaymentService.confirmPayment`를 호출합니다.
    4. `PaymentService`는 결제 수단에 맞는 `PaymentProcessor`를 통해 PG사 최종 승인 및 내부 결제 데이터 기록을 수행합니다.
    5. 성공/실패 여부에 따라 `PaymentReturnResponse`를 반환합니다.
- **성공 조건**: PG사 최종 승인 및 내부 결제 데이터 기록 성공 시 200 OK와 `success: true`, `Payment` 객체 포함한 `PaymentReturnResponse` 반환.
- **실패 조건**: PG사 최종 승인 실패, 포인트 부족, 기타 내부 오류 발생 시 200 OK와 `success: false`, `errorMessage` 포함한 `PaymentReturnResponse` 반환 (PG사 리턴 규약에 따라 200 OK로 응답).

### 2.4.3. 결제 조회 (단건)
- **목적**: 특정 결제 ID에 해당하는 결제 정보를 조회합니다.
- **입력**: `paymentId` (String)
- **출력**: `Payment` 객체
- **비즈니스 규칙**: `paymentId`에 해당하는 결제가 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `paymentId`를 전달하여 결제 정보 조회를 요청합니다.
    2. 시스템은 해당 `paymentId`의 결제 정보를 찾아 반환합니다.
- **성공 조건**: `paymentId`에 해당하는 결제 정보 반환 시 200 OK.
- **실패 조건**: `paymentId`에 해당하는 결제가 없을 경우 404 Not Found.

### 2.4.4. 결제 목록 조회
- **목적**: 시스템에 기록된 모든 결제 내역을 조회합니다.
- **입력**: 없음
- **출력**: `Payment` 객체 리스트
- **비즈니스 규칙**: 없음
- **사용자 흐름**:
    1. 시스템에 결제 목록 조회를 요청합니다.
    2. 시스템은 모든 결제 정보를 반환합니다.
- **성공 조건**: 결제 목록 반환 시 200 OK.
- **실패 조건**: (현재 없음, 내부 서버 오류 시 500 Internal Server Error)

## 2.5. 포인트 관리

### 2.5.1. 회원별 포인트 내역 조회 (페이징 포함)
- **목적**: 특정 회원의 포인트 적립, 사용, 환불 등 모든 변동 내역을 조회합니다.
- **입력**: `memberId` (Long), `page` (int, 기본값 0), `size` (int, 기본값 10)
- **출력**: `PointHistory` 객체 리스트
- **비즈니스 규칙**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `page`와 `size`를 이용하여 페이징 처리합니다.
    - 최신 내역부터 조회됩니다.
- **사용자 흐름**:
    1. 사용자가 `memberId`, `page`, `size`를 전달하여 포인트 내역 조회를 요청합니다.
    2. 시스템은 해당 회원의 포인트 변동 내역을 페이징하여 반환합니다.
- **성공 조건**: `PointHistory` 리스트 반환 시 200 OK.
- **실패 조건**: `memberId`에 해당하는 회원이 없거나, 내부 서버 오류 시 400 Bad Request 또는 500 Internal Server Error.

### 2.5.2. 회원별 포인트 통계 조회
- **목적**: 특정 회원의 현재 포인트 잔액, 총 적립/사용/환불 포인트 및 각 거래 횟수 등 통계 정보를 조회합니다.
- **입력**: `memberId` (Long)
- **출력**: `PointStatistics` 객체
- **비즈니스 규칙**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `PointHistory` 데이터를 기반으로 통계를 집계합니다.
- **사용자 흐름**:
    1. 사용자가 `memberId`를 전달하여 포인트 통계 조회를 요청합니다.
    2. 시스템은 해당 회원의 모든 포인트 내역을 집계하여 통계 정보를 반환합니다.
- **성공 조건**: `PointStatistics` 객체 반환 시 200 OK.
- **실패 조건**: `memberId`에 해당하는 회원이 없거나, 내부 서버 오류 시 500 Internal Server Error.

### 2.5.3. 회원별 특정 거래 타입 포인트 내역 조회
- **목적**: 특정 회원의 포인트 내역 중 특정 거래 유형(예: 적립, 사용, 환불)에 해당하는 내역만 조회합니다.
- **입력**: `memberId` (Long), `transactionType` (String, `EARN`, `USE`, `REFUND`)
- **출력**: `PointHistory` 객체 리스트
- **비즈니스 규칙**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `transactionType`은 `TransactionType` Enum에 정의된 유효한 값이어야 합니다.
- **사용자 흐름**:
    1. 사용자가 `memberId`와 `transactionType`을 전달하여 포인트 내역 조회를 요청합니다.
    2. 시스템은 해당 회원의 특정 거래 유형 포인트 내역을 반환합니다.
- **성공 조건**: `PointHistory` 리스트 반환 시 200 OK.
- **실패 조건**: `memberId`에 해당하는 회원이 없거나, 유효하지 않은 `transactionType`, 내부 서버 오류 시 400 Bad Request 또는 500 Internal Server Error.

### 2.5.4. 특정 거래 관련 포인트 내역 조회
- **목적**: 특정 참조 유형(예: 결제, 취소) 및 참조 ID(예: `payment_id`, `order_id`)와 관련된 포인트 내역을 조회합니다.
- **입력**: `referenceType` (String), `referenceId` (String)
- **출력**: `PointHistory` 객체 리스트
- **비즈니스 규칙**: `referenceType`과 `referenceId`에 해당하는 포인트 내역이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `referenceType`과 `referenceId`를 전달하여 포인트 내역 조회를 요청합니다.
    2. 시스템은 해당 참조와 관련된 포인트 내역을 반환합니다.
- **성공 조건**: `PointHistory` 리스트 반환 시 200 OK.
- **실패 조건**: 내부 서버 오류 시 500 Internal Server Error.

### 2.5.5. 전체 포인트 내역 조회 (관리자용)
- **목적**: 시스템에 기록된 모든 회원의 포인트 변동 내역을 조회합니다.
- **입력**: 없음
- **출력**: `PointHistory` 객체 리스트
- **비즈니스 규칙**: 관리자 권한이 필요합니다 (현재는 권한 검증 로직 없음).
- **사용자 흐름**:
    1. 관리자가 전체 포인트 내역 조회를 요청합니다.
    2. 시스템은 모든 포인트 변동 내역을 반환합니다.
- **성공 조건**: `PointHistory` 리스트 반환 시 200 OK.
- **실패 조건**: (현재 없음, 내부 서버 오류 시 500 Internal Server Error)

### 2.5.6. 특정 포인트 내역 조회
- **목적**: 특정 `pointHistoryId`에 해당하는 포인트 내역을 조회합니다.
- **입력**: `pointHistoryId` (Long)
- **출력**: `PointHistory` 객체
- **비즈니스 규칙**: `pointHistoryId`에 해당하는 내역이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `pointHistoryId`를 전달하여 포인트 내역 조회를 요청합니다.
    2. 시스템은 해당 `pointHistoryId`의 포인트 내역을 찾아 반환합니다.
- **성공 조건**: `pointHistoryId`에 해당하는 포인트 내역 반환 시 200 OK.
- **실패 조건**: `pointHistoryId`에 해당하는 내역이 없을 경우 404 Not Found.

### 2.5.7. 리워드 포인트 조회 (단건)
- **목적**: 특정 `rewardPointsId`에 해당하는 리워드 포인트 정보를 조회합니다.
- **입력**: `rewardPointsId` (Long)
- **출력**: `RewardPoints` 객체
- **비즈니스 규칙**: `rewardPointsId`에 해당하는 리워드 포인트가 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `rewardPointsId`를 전달하여 리워드 포인트 조회를 요청합니다.
    2. 시스템은 해당 `rewardPointsId`의 리워드 포인트 정보를 찾아 반환합니다.
- **성공 조건**: `rewardPointsId`에 해당하는 리워드 포인트 정보 반환 시 200 OK.
- **실패 조건**: `rewardPointsId`에 해당하는 리워드 포인트가 없을 경우 404 Not Found.

### 2.5.8. 회원별 리워드 포인트 조회
- **목적**: 특정 회원의 현재 리워드 포인트 잔액을 조회합니다.
- **입력**: `memberId` (Long)
- **출력**: `RewardPoints` 객체
- **비즈니스 규칙**: `memberId`에 해당하는 회원이 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `memberId`를 전달하여 회원별 리워드 포인트 조회를 요청합니다.
    2. 시스템은 해당 회원의 리워드 포인트 정보를 찾아 반환합니다.
- **성공 조건**: `memberId`에 해당하는 리워드 포인트 정보 반환 시 200 OK.
- **실패 조건**: `memberId`에 해당하는 리워드 포인트가 없을 경우 404 Not Found.

### 2.5.9. 리워드 포인트 추가 (관리자용)
- **목적**: 특정 회원에게 수동으로 리워드 포인트를 추가합니다.
- **입력**: `RewardPointsRequest` 객체 (memberId, points)
- **출력**: 업데이트된 `RewardPoints` 객체
- **비즈니스 규칙**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `points`는 0보다 커야 합니다.
    - `REWARD_POINTS` 테이블의 해당 회원의 `points`를 증가시키고 `lastUpdated`를 갱신합니다.
    - `POINT_HISTORY`에 `EARN` 타입의 내역을 기록합니다.
- **사용자 흐름**:
    1. 관리자가 `memberId`와 추가할 `points`를 입력합니다.
    2. 시스템은 해당 회원의 리워드 포인트를 업데이트하고, 포인트 내역을 기록한 후 업데이트된 정보를 반환합니다.
- **성공 조건**: 포인트 추가 및 내역 기록 성공 시 200 OK와 업데이트된 `RewardPoints` 객체 반환.
- **실패 조건**: `memberId`에 해당하는 회원이 없거나, `points`가 유효하지 않은 경우 400 Bad Request.

### 2.5.10. 리워드 포인트 사용
- **목적**: 특정 회원의 리워드 포인트를 사용합니다.
- **입력**: `RewardPointsRequest` 객체 (memberId, points)
- **출력**: 업데이트된 `RewardPoints` 객체
- **비즈니스 규칙**:
    - `memberId`에 해당하는 회원이 존재해야 합니다.
    - `points`는 0보다 커야 합니다.
    - 회원이 사용하려는 `points`보다 적은 포인트를 가지고 있을 경우 `IllegalStateException` 발생.
    - `REWARD_POINTS` 테이블의 해당 회원의 `points`를 감소시키고 `lastUpdated`를 갱신합니다.
    - `POINT_HISTORY`에 `USE` 타입의 내역을 기록합니다.
- **사용자 흐름**:
    1. 시스템에 `memberId`와 사용할 `points`를 전달하여 포인트 사용을 요청합니다.
    2. 시스템은 해당 회원의 리워드 포인트를 업데이트하고, 포인트 내역을 기록한 후 업데이트된 정보를 반환합니다.
- **성공 조건**: 포인트 사용 및 내역 기록 성공 시 200 OK와 업데이트된 `RewardPoints` 객체 반환.
- **실패 조건**: `memberId`에 해당하는 회원이 없거나, `points`가 유효하지 않은 경우, 포인트 잔액 부족 시 400 Bad Request.

## 2.6. 결제 인터페이스 요청 로그

### 2.6.1. 결제 인터페이스 로그 생성
- **목적**: PG사와의 통신 요청 및 응답 데이터를 기록합니다.
- **입력**: `PaymentInterfaceRequestLog` 객체 (paymentId, requestType, requestPayload, responsePayload)
- **출력**: 생성된 `PaymentInterfaceRequestLog` 객체
- **비즈니스 규칙**:
    - `paymentId`, `requestType`, `requestPayload`, `responsePayload`는 필수 값입니다.
    - `timestamp`는 현재 시각으로 자동 설정됩니다.
- **사용자 흐름**:
    1. PG사와의 통신 발생 시, 시스템은 요청/응답 데이터를 포함한 `PaymentInterfaceRequestLog`를 생성합니다.
    2. 시스템은 로그 레코드를 DB에 저장하고 생성된 로그 객체를 반환합니다.
- **성공 조건**: 로그 기록 성공 시 200 OK와 생성된 `PaymentInterfaceRequestLog` 객체 반환.
- **실패 조건**: 필수 값 누락 등 유효성 검증 실패 시 400 Bad Request.

### 2.6.2. 결제 인터페이스 로그 조회 (단건)
- **목적**: 특정 `logId`에 해당하는 결제 인터페이스 로그를 조회합니다.
- **입력**: `logId` (Long)
- **출력**: `PaymentInterfaceRequestLog` 객체
- **비즈니스 규칙**: `logId`에 해당하는 로그가 존재해야 합니다.
- **사용자 흐름**:
    1. 시스템에 `logId`를 전달하여 로그 조회를 요청합니다.
    2. 시스템은 해당 `logId`의 로그 정보를 찾아 반환합니다.
- **성공 조건**: `logId`에 해당하는 로그 정보 반환 시 200 OK.
- **실패 조건**: `logId`에 해당하는 로그가 없을 경우 404 Not Found.

### 2.6.3. 결제 인터페이스 로그 목록 조회
- **목적**: 시스템에 기록된 모든 결제 인터페이스 로그를 조회합니다.
- **입력**: 없음
- **출력**: `PaymentInterfaceRequestLog` 객체 리스트
- **비즈니스 규칙**: 없음
- **사용자 흐름**:
    1. 시스템에 로그 목록 조회를 요청합니다.
    2. 시스템은 모든 로그 정보를 반환합니다.
- **성공 조건**: 로그 목록 반환 시 200 OK.
- **실패 조건**: (현재 없음, 내부 서버 오류 시 500 Internal Server Error)
