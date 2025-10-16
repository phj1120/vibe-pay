# NICEPAY API Technical Specification

## 1. 개요
- **목적**: 본 문서는 VibePay 백엔드 시스템에서 나이스페이(NICEPAY) PG사와의 연동에 사용되는 주요 API에 대한 기술 사양을 정의합니다. 이 문서는 나이스페이 결제 초기화, 승인, 취소, 망취소 API의 요청 및 응답 구조, 그리고 연동 시 필요한 비즈니스 로직을 설명합니다.
- **System Design에서의 역할**: VibePay 결제 모듈의 `NicePayAdapter`를 통해 호출되며, 실제 결제 처리의 핵심적인 외부 연동을 담당합니다.
- **관련 컴포넌트 및 의존성**:
    - `NicePayAdapter`: 나이스페이 API 호출을 캡슐화하는 어댑터
    - `PaymentInitiateRequest`, `PaymentConfirmRequest`, `PaymentCancelRequest`, `PaymentNetCancelRequest`: VibePay 내부에서 나이스페이 API 호출을 위해 사용되는 요청 DTO
    - `PaymentInitResponse`, `PaymentConfirmResponse`: VibePay 내부에서 나이스페이 API 호출 결과를 추상화한 응답 DTO
    - `NicePayConfirmRequest`, `NicePayConfirmResponse`, `NicePayCancelRequest`, `NicePayCancelResponse`, `NicePayNetCancelRequest`: 나이스페이 API의 실제 응답/요청 전문 DTO
    - `WebClientUtil`: 실제 HTTP 통신을 위한 유틸리티
    - `MessageDigest`: 서명(SignData) 생성에 사용되는 해싱 유틸리티

## 2. API 명세

### 2.1. 결제 초기화 (나이스페이 결제창 연동)
- **나이스페이 결제창 URL**: `https://web.nicepay.co.kr/v3/v3Payment.jsp`
- **HTTP Method**: `POST` (프론트엔드 -> 나이스페이 결제창)
- **요청 파라미터 (프론트엔드 -> 나이스페이 결제창)**:
    - `mid` (String, 필수): 가맹점 ID (나이스페이에서 발급)
    - `moid` (String, 필수): 주문 ID (가맹점에서 생성한 고유 주문 번호)
    - `amt` (Long, 필수): 결제 금액
    - `goodName` (String, 필수): 상품명
    - `buyerName` (String, 필수): 구매자명
    - `buyerEmail` (String, 필수): 구매자 이메일
    - `buyerTel` (String, 필수): 구매자 전화번호
    - `ediDate` (String, 필수): 전문 생성 일시 (`YYYYMMDDHHMMSS` 형식)
    - `SignData` (String, 필수): 서명 데이터 (`EdiDate + MID + Amt + MerchantKey` 문자열을 SHA-256 해싱)
    - `returnUrl` (String, 필수): 결제 완료 후 나이스페이가 클라이언트를 리다이렉트할 URL
    - `cancelUrl` (String, 필수): 결제 취소 시 나이스페이가 클라이언트를 리다이렉트할 URL
    - `version` (String, 필수): API 버전 ("1.0")
    - `currency` (String, 필수): 통화 단위 ("KRW")
- **요청 예시 (HTML Form Data)**:
    ```html
    <form name="nicepay" method="POST" action="https://web.nicepay.co.kr/v3/v3Payment.jsp">
        <input type="hidden" name="mid" value="nicepayTest01">
        <input type="hidden" name="moid" value="ORDER_202510160001">
        <input type="hidden" name="amt" value="15000">
        <input type="hidden" name="goodName" value="VibePay 상품">
        <input type="hidden" name="buyerName" value="홍길동">
        <input type="hidden" name="buyerEmail" value="hong@example.com">
        <input type="hidden" name="buyerTel" value="01012345678">
        <input type="hidden" name="ediDate" value="20251016120000">
        <input type="hidden" name="SignData" value="[SHA-256 해싱된 서명 데이터]">
        <input type="hidden" name="returnUrl" value="http://localhost:3000/payment/nicepay/return">
        <input type="hidden" name="cancelUrl" value="http://localhost:3000/payment/nicepay/cancel">
        <input type="hidden" name="version" value="1.0">
        <input type="hidden" name="currency" value="KRW">
    </form>
    ```
- **프론트엔드 처리 방식**: 프론트엔드는 백엔드로부터 받은 위 파라미터들을 사용하여 나이스페이 결제창을 팝업 또는 리다이렉트 방식으로 띄웁니다.

### 2.2. 결제 승인 (최종 승인)
- **API Endpoint**: 나이스페이 결제창으로부터 `returnUrl`을 통해 전달받은 `nextAppUrl` (예: `https://webapi.nicepay.co.kr/v3/v3Payment.jsp`)
- **HTTP Method**: `POST`
- **요청 파라미터 (백엔드 -> 나이스페이)**:
    - `TID` (String, 필수): 나이스페이 거래 ID (인증 응답의 `TxTid`)
    - `AuthToken` (String, 필수): 나이스페이 결제창으로부터 `returnUrl`을 통해 전달받은 인증 토큰
    - `MID` (String, 필수): 가맹점 ID
    - `Amt` (String, 필수): 결제 금액 (인증 응답의 `Amt`)
    - `EdiDate` (String, 필수): 전문 생성 일시 (`YYYYMMDDHHMMSS` 형식)
    - `SignData` (String, 필수): 서명 데이터 (`AuthToken + MID + Amt + EdiDate + MerchantKey`를 SHA-256 해싱 값)
- **요청 예시 (JSON)**:
    ```json
    {
        "TID": "nicepayTest_20251016103500_123456",
        "AuthToken": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "MID": "nicepayTest01",
        "Amt": "15000",
        "EdiDate": "20251016120000",
        "SignData": "[SHA-256 해싱된 서명 데이터]"
    }
    ```
- **응답 구조**: `NicePayConfirmResponse` (JSON)
    ```json
    {
        "resultCode": "3001",
        "resultMsg": "정상 승인",
        "TID": "nicepayTest_20251016103500_123456",
        "MID": "nicepayTest01",
        "Amt": "15000",
        "Signature": "[SHA-256 해싱된 응답 서명]",
        "AuthCode": "12345678",
        "Moid": "ORDER_202510160001",
        "CardNo": "************1234",
        "CardQuota": "00",
        "CardCode": "04",
        "CardName": "국민카드"
    }
    ```
- **Success Response**: `resultCode`가 "3001"이고, 응답 `Signature` 검증 성공.
- **Error Response**: `resultCode`가 "3001"이 아니거나, 응답 `Signature` 검증 실패.

### 2.3. 결제 취소
- **API Endpoint**: `https://webapi.nicepay.co.kr/webapi/cancel_process.jsp`
- **HTTP Method**: `POST`
- **요청 파라미터 (백엔드 -> 나이스페이)**:
    - `TID` (String, 필수): 취소할 거래 ID
    - `MID` (String, 필수): 가맹점 ID
    - `CancelAmt` (String, 필수): 취소 금액
    - `CancelMsg` (String, 필수): 취소 사유
    - `PartialCancelCode` (String, 필수): "0" (전체 취소) 또는 "1" (부분 취소)
    - `EdiDate` (String, 필수): 전문 생성 일시 (`YYYYMMDDHHMMSS` 형식)
    - `SignData` (String, 필수): 서명 데이터 (`MID + CancelAmt + EdiDate + MerchantKey`를 SHA-256 해싱 값)
    - `CharSet` (String, 필수): "utf-8"
    - `Moid` (String, 선택): 주문 ID
- **요청 예시 (JSON)**:
    ```json
    {
        "TID": "nicepayTest_20251016103500_123456",
        "MID": "nicepayTest01",
        "CancelAmt": "15000",
        "CancelMsg": "고객 변심",
        "PartialCancelCode": "0",
        "EdiDate": "20251016120000",
        "SignData": "[SHA-256 해싱된 서명 데이터]",
        "CharSet": "utf-8",
        "Moid": "ORDER_202510160001"
    }
    ```
- **응답 구조**: `NicePayCancelResponse` (JSON)
    ```json
    {
        "resultCode": "2001",
        "resultMsg": "취소 성공",
        "TID": "nicepayTest_20251016103500_123456",
        "CancelAmt": "15000",
        "CancelDate": "20251016",
        "CancelTime": "120500"
    }
    ```
- **Success Response**: `resultCode`가 "2001".
- **Error Response**: `resultCode`가 "2001"이 아님.

### 2.4. 망취소
- **API Endpoint**: 나이스페이 결제창으로부터 `returnUrl`을 통해 전달받은 `netCancelUrl` (예: `https://webapi.nicepay.co.kr/v3/v3Payment.jsp`)
- **HTTP Method**: `POST`
- **요청 파라미터 (백엔드 -> 나이스페이)**:
    - `TID` (String, 필수): 나이스페이 거래 ID
    - `AuthToken` (String, 필수): 나이스페이 결제창으로부터 `returnUrl`을 통해 전달받은 인증 토큰
    - `MID` (String, 필수): 가맹점 ID
    - `Amt` (String, 필수): 금액
    - `EdiDate` (String, 필수): 전문 생성 일시 (`YYYYMMDDHHMMSS` 형식)
    - `NetCancel` (String, 필수): "1" (고정값, 망취소 여부)
    - `SignData` (String, 필수): 서명 데이터 (`AuthToken + MID + Amt + EdiDate + MerchantKey`를 SHA-256 해싱 값)
- **요청 예시 (JSON)**:
    ```json
    {
        "TID": "nicepayTest_20251016103500_123456",
        "AuthToken": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "MID": "nicepayTest01",
        "Amt": "15000",
        "EdiDate": "20251016120000",
        "NetCancel": "1",
        "SignData": "[SHA-256 해싱된 서명 데이터]"
    }
    ```
- **응답 구조**: `String` (나이스페이 망취소 API는 응답이 단순 문자열로 올 수 있음)
    - **예시**: `OK` 또는 `FAIL` 등의 단순 문자열
- **Success Response**: HTTP Status 2xx.
- **Error Response**: HTTP Status 4xx, 5xx 또는 응답 내용에 실패 메시지 포함.

## 3. 연동 상세

### 3.1. 서명(SignData) 생성 로직 (나이스페이 요구사항)
- **결제 초기화 시 `SignData` 생성**: `EdiDate + MID + Amt + MerchantKey` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("20251016120000{MID}10000{MerchantKey}")`
- **결제 승인 시 `SignData` 생성**: `AuthToken + MID + Amt + EdiDate + MerchantKey` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("{AuthToken}{MID}1000020251016120000{MerchantKey}")`
- **응답 `Signature` 검증용**: `TID + MID + Amt + MerchantKey` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("{TID}{MID}10000{MerchantKey}")`
- **결제 취소 시 `SignData` 생성**: `MID + CancelAmt + EdiDate + MerchantKey` 문자열을 SHA-256 해싱.
    - **예시**: `SHA-256("{MID}1000020251016120000{MerchantKey}")`
- **해싱 유틸리티**: `java.security.MessageDigest`를 사용하여 SHA-256 해싱을 수행합니다.

### 3.2. 에러 코드 및 처리
- 나이스페이 `resultCode`가 "3001" (결제 승인 성공) 또는 "2001" (결제 취소 성공)이 아닌 경우 실패로 간주합니다.
- 상세 에러 메시지는 `resultMsg` 필드를 통해 확인합니다.

### 3.3. 특이사항 및 주의사항
- **결제창 연동 방식**: 나이스페이는 클라이언트(프론트엔드)에서 직접 나이스페이 결제창을 팝업 또는 리다이렉트 방식으로 호출합니다. 백엔드는 결제창을 띄우기 위한 파라미터만 제공합니다.
- **최종 승인 방식**: 나이스페이 결제창에서 결제 완료 후 가맹점에서 설정한 `returnUrl`로 클라이언트를 리다이렉트합니다. 이때 `AuthToken`, `TxTid`, `NextAppURL` 등의 파라미터를 전달하며, 가맹점 백엔드는 이 `NextAppURL`로 최종 승인 API를 호출해야 합니다.
- **망취소**: 나이스페이 결제 승인 후 가맹점 내부 로직 오류 등으로 인해 결제를 취소해야 할 경우, 나이스페이 결제창으로부터 전달받은 `netCancelUrl`과 `AuthToken`, `TID` 등을 사용하여 망취소 API를 호출할 수 있습니다.
- **응답 `Signature` 검증**: 나이스페이는 결제 승인 응답에 `Signature` 필드를 포함하며, 가맹점 백엔드에서 이를 검증하여 응답의 무결성을 확인해야 합니다.
