# INICIS API Technical Specification

## 1. 개요
- **목적**: 본 문서는 VibePay 백엔드 시스템에서 이니시스(INICIS) PG사와의 연동에 사용되는 주요 API에 대한 기술 사양을 정의합니다. 이 문서는 이니시스 결제 초기화, 승인, 취소, 망취소 API의 요청 및 응답 구조, 그리고 연동 시 필요한 비즈니스 로직을 설명합니다.
- **System Design에서의 역할**: VibePay 결제 모듈의 `InicisAdapter`를 통해 호출되며, 실제 결제 처리의 핵심적인 외부 연동을 담당합니다.
- **관련 컴포넌트 및 의존성**:
    - `InicisAdapter`: 이니시스 API 호출을 캡슐화하는 어댑터
    - `PaymentInitiateRequest`, `PaymentConfirmRequest`, `PaymentCancelRequest`, `PaymentNetCancelRequest`: VibePay 내부에서 이니시스 API 호출을 위해 사용되는 요청 DTO
    - `PaymentInitResponse`, `PaymentConfirmResponse`: VibePay 내부에서 이니시스 API 호출 결과를 추상화한 응답 DTO
    - `InicisApprovalResponse`, `InicisRefundRequest`, `InicisRefundResponse`: 이니시스 API의 실제 응답/요청 전문 DTO
    - `WebClientUtil`, `RestTemplate`: 실제 HTTP 통신을 위한 유틸리티
    - `HashUtils`: 서명(Signature) 생성에 사용되는 해싱 유틸리티

## 2. API 명세

### 2.1. 결제 초기화 (이니시스 결제창 연동)
- **이니시스 결제창 URL**: `https://stgstdpay.inicis.com/stdpay/pay.ini` (테스트 환경)
    - **운영 환경**: `https://stdpay.inicis.com/stdpay/pay.ini`
- **HTTP Method**: `POST` (프론트엔드 -> 이니시스 결제창)
- **요청 파라미터 (프론트엔드 -> 이니시스 결제창)**:
    - `mid` (String, 필수): 가맹점 ID (이니시스에서 발급)
    - `oid` (String, 필수): 주문 ID (가맹점에서 생성한 고유 주문 번호)
    - `price` (Long, 필수): 결제 금액
    - `goodName` (String, 필수): 상품명
    - `buyerName` (String, 필수): 구매자명
    - `buyerTel` (String, 필수): 구매자 전화번호
    - `buyerEmail` (String, 필수): 구매자 이메일
    - `timestamp` (String, 필수): 요청 시각 (`YYYYMMDDHHMMSS` 형식)
    - `mKey` (String, 필수): 이니시스 MKey (이니시스 `signKey`를 SHA-256 해싱한 값)
    - `signature` (String, 필수): 서명 데이터 (`oid={주문번호}&price={결제금액}&timestamp={타임스탬프}` 문자열을 SHA-256 해싱)
    - `verification` (String, 필수): 검증 데이터 (`oid={주문번호}&price={결제금액}&signKey={이니시스_signKey}&timestamp={타임스탬프}` 문자열을 SHA-256 해싱)
    - `returnUrl` (String, 필수): 결제 완료 후 이니시스가 클라이언트를 리다이렉트할 URL
    - `closeUrl` (String, 필수): 결제창 닫기 URL
    - `version` (String, 필수): API 버전 ("1.0")
    - `currency` (String, 필수): 통화 단위 ("WON")
    - `gopaymethod` (String, 선택): 결제 수단 ("Card" 등)
    - `acceptmethod` (String, 선택): 결제 허용 방식 ("below1000" 등)
- **요청 예시 (HTML Form Data)**:
    ```html
    <form name="ini" method="POST" action="https://stgstdpay.inicis.com/stdpay/pay.ini">
        <input type="hidden" name="mid" value="INIpayTest">
        <input type="hidden" name="oid" value="ORDER_202510160001">
        <input type="hidden" name="price" value="15000">
        <input type="hidden" name="goodName" value="VibePay 상품">
        <input type="hidden" name="buyerName" value="홍길동">
        <input type="hidden" name="buyerTel" value="01012345678">
        <input type="hidden" name="buyerEmail" value="hong@example.com">
        <input type="hidden" name="timestamp" value="20251016103000">
        <input type="hidden" name="mKey" value="[SHA-256 해싱된 이니시스_signKey]">
        <input type="hidden" name="signature" value="[SHA-256 해싱된 서명 데이터]">
        <input type="hidden" name="verification" value="[SHA-256 해싱된 검증 데이터]">
        <input type="hidden" name="returnUrl" value="http://localhost:3000/payment/return">
        <input type="hidden" name="closeUrl" value="http://localhost:3000/payment/close">
        <input type="hidden" name="version" value="1.0">
        <input type="hidden" name="currency" value="WON">
        <input type="hidden" name="gopaymethod" value="Card">
        <input type="hidden" name="acceptmethod" value="below1000">
    </form>
    ```
- **프론트엔드 처리 방식**: 프론트엔드는 백엔드로부터 받은 위 파라미터들을 사용하여 이니시스 결제창을 팝업 또는 리다이렉트 방식으로 띄웁니다.

### 2.2. 결제 승인 (최종 승인)
- **API Endpoint**: 이니시스 결제창으로부터 `returnUrl`을 통해 전달받은 `authUrl` (예: `https://stgstdpay.inicis.com/stdpay/web/INIStdPayAppr.ini`)
    - **운영 환경**: `https://stdpay.inicis.com/stdpay/web/INIStdPayAppr.ini`
- **HTTP Method**: `POST`
- **요청 파라미터 (백엔드 -> 이니시스)**:
    - `mid` (String, 필수): 가맹점 ID
    - `authToken` (String, 필수): 이니시스 결제창으로부터 `returnUrl`을 통해 전달받은 인증 토큰
    - `signature` (String, 필수): 서명 데이터 (`authToken`과 `timestamp`를 이용한 SHA-256 해싱 값)
    - `timestamp` (String, 필수): 요청 시각 (밀리초 단위)
    - `charset` (String, 필수): "UTF-8"
    - `format` (String, 필수): "JSON"
- **요청 예시 (Form Data)**:
    ```
    mid=INIpayTest&authToken=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx&signature=yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy&timestamp=1678886400000&charset=UTF-8&format=JSON
    ```
- **응답 구조**: `InicisApprovalResponse` (JSON)
    ```json
    {
        "resultCode": "0000",
        "resultMsg": "결제 성공",
        "TotPrice": 15000,
        "tid": "INIpayTest_20251016103500_123456",
        "MOID": "ORDER_202510160001",
        "applDate": "20251016",
        "applTime": "103500",
        "applNum": "12345678",
        "payMethod": "Card",
        "CARD_Quota": "00",
        "CARD_IssuerName": "국민카드",
        "CARD_Num": "************1234",
        "CARD_ApplPrice": "15000",
        "CARD_Code": "04",
        "CARD_BankCode": "04",
        "CARD_Interest": "N",
        "P_FN_NM": "국민카드",
        "CARD_PurchaseName": "국민카드"
    }
    ```
- **Success Response**: `resultCode`가 "0000"이고, `TotPrice`가 요청 금액과 일치.
- **Error Response**: `resultCode`가 "0000"이 아니거나, 금액 불일치.

### 2.3. 결제 취소
- **API Endpoint**: 이니시스 환불 API URL (예: `https://iniapi.inicis.com/api/v1/refund`)
- **HTTP Method**: `POST`
- **요청 파라미터 (백엔드 -> 이니시스)**:
    - `mid` (String, 필수): 가맹점 ID
    - `type` (String, 필수): "refund" (고정값)
    - `timestamp` (String, 필수): 요청 시각 (`YYYYMMDDhhmmss` 형식)
    - `clientIp` (String, 필수): 요청 서버 IP
    - `hashData` (String, 필수): 서명 데이터 (`INIAPIKey + mid + type + timestamp + data(JSON 문자열)`을 SHA-512 해싱)
    - `data` (JSON Object, 필수): `RefundData` 객체
        - `tid` (String, 필수): 취소할 거래 ID
        - `msg` (String, 필수): 취소 사유
- **요청 예시 (JSON)**:
    ```json
    {
        "mid": "INIpayTest",
        "type": "refund",
        "timestamp": "20251016120000",
        "clientIp": "127.0.0.1",
        "hashData": "[SHA-512 해싱된 서명 데이터]",
        "data": {
            "tid": "INIpayTest_20251016103500_123456",
            "msg": "고객 변심"
        }
    }
    ```
- **응답 구조**: `InicisRefundResponse` (JSON)
    ```json
    {
        "resultCode": "00",
        "resultMsg": "부분취소 성공",
        "tid": "INIpayTest_20251016103500_123456",
        "cancelDate": "20251016",
        "cancelTime": "120500",
        "CSHR_ResultCode": "00",
        "CSHR_ResultMsg": "정상처리"
    }
    ```
- **Success Response**: `resultCode`가 "00".
- **Error Response**: `resultCode`가 "00"이 아님.

### 2.4. 망취소
- **API Endpoint**: 이니시스 결제창으로부터 `returnUrl`을 통해 전달받은 `netCancelUrl` (예: `https://stgstdpay.inicis.com/stdpay/web/INIStdPayNetCancel.ini`)
- **HTTP Method**: `POST`
- **요청 파라미터 (백엔드 -> 이니시스)**:
    - `mid` (String, 필수): 가맹점 ID
    - `authToken` (String, 필수): 이니시스 결제창으로부터 `returnUrl`을 통해 전달받은 인증 토큰
    - `signature` (String, 필수): 서명 데이터 (`authToken`과 `timestamp`를 이용한 SHA-256 해싱 값)
    - `timestamp` (String, 필수): 요청 시각 (밀리초 단위)
    - `charset` (String, 필수): "UTF-8"
    - `format` (String, 필수): "JSON"
- **요청 예시 (Form Data)**:
    ```
    mid=INIpayTest&authToken=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx&signature=yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy&timestamp=1678886400000&charset=UTF-8&format=JSON
    ```
- **응답 구조**: `String` (이니시스 망취소 API는 응답이 단순 문자열로 올 수 있음)
    - **예시**: `OK` 또는 `FAIL` 등의 단순 문자열
- **Success Response**: HTTP Status 2xx.
- **Error Response**: HTTP Status 4xx, 5xx 또는 응답 내용에 실패 메시지 포함.

## 3. 연동 상세

### 3.1. 서명(Signature) 및 검증(Verification) 로직 (이니시스 요구사항)
- **결제 초기화 시 `signature` 생성**: `oid={주문번호}&price={결제금액}&timestamp={타임스탬프}` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("oid=ORDER_123&price=10000&timestamp=20251016120000")`
- **결제 초기화 시 `mKey` 생성**: 이니시스에서 발급받은 `signKey`를 SHA-256 해싱.
    - **예시**: `SHA-256("{이니시스_signKey}")`
- **결제 초기화 시 `verification` 생성**: `oid={주문번호}&price={결제금액}&signKey={이니시스_signKey}&timestamp={타임스탬프}` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("oid=ORDER_123&price=10000&signKey={이니시스_signKey}&timestamp=20251016120000")`
- **결제 승인 시 `signature` 생성**: `authToken={인증토큰}&timestamp={타임스탬프}` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("authToken=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx&timestamp=1678886400000")`
- **결제 취소 시 `hashData` 생성 (v2 API)**: `INIAPIKey + mid + type + timestamp + data(JSON 문자열)`을 SHA-512 해싱.
    - **예시**: `SHA-512("{이니시스_API_Key}INIpayTestrefund20251016120000{\"tid\":\"TID_123\",\"msg\":\"취소\"}")`
- **망취소 시 `signature` 생성**: `authToken={인증토큰}&timestamp={타임스탬프}` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("authToken=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx&timestamp=1678886400000")`

### 3.2. 에러 코드 및 처리
- 이니시스 `resultCode`가 "0000" (결제 승인) 또는 "00" (결제 취소)이 아닌 경우 실패로 간주합니다.
- 상세 에러 메시지는 `resultMsg` 필드를 통해 확인합니다.

### 3.3. 특이사항 및 주의사항
- **결제창 연동 방식**: 이니시스는 클라이언트(프론트엔드)에서 직접 이니시스 결제창을 팝업 또는 리다이렉트 방식으로 호출합니다. 백엔드는 결제창을 띄우기 위한 파라미터만 제공합니다.
- **최종 승인 방식**: 이니시스 결제창에서 결제 완료 후 가맹점에서 설정한 `returnUrl`로 클라이언트를 리다이렉트합니다. 이때 `authToken`과 `authUrl` 등의 파라미터를 전달하며, 가맹점 백엔드는 이 `authUrl`로 최종 승인 API를 호출해야 합니다.
- **망취소**: 이니시스 결제 승인 후 가맹점 내부 로직 오류 등으로 인해 결제를 취소해야 할 경우, 이니시스 결제창으로부터 전달받은 `netCancelUrl`과 `authToken`을 사용하여 망취소 API를 호출할 수 있습니다.
- **취소 요청 `clientIp`**: 이니시스 취소 요청 시 `clientIp` 파라미터는 요청을 보내는 서버의 IP 주소를 전달해야 합니다.
- **취소 요청 `RefundData` JSON 포맷**: 이니시스 취소 요청 시 `data` 필드는 `RefundData` 객체를 JSON 문자열로 변환하여 전달해야 하며, 이니시스 API 문서에서 요구하는 정확한 JSON 포맷을 준수해야 합니다.
