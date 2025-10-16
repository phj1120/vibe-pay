# 프론트엔드 PG 연동 상세 구현 가이드 (이니시스 vs. 나이스페이)

이 문서는 Vibe Pay 프로젝트의 프론트엔드에서 **이니시스**와 **나이스페이**라는 서로 다른 두 결제 게이트웨이(PG)를 어떻게 구현했는지, 그 구체적인 차이점과 핵심 로직을 상세하게 설명합니다.

## 전제: 백엔드의 역할

프론트엔드는 `POST /api/payments/initiate` API를 호출하기만 하면 됩니다. 어떤 PG사를 사용할지, 그에 필요한 암호화된 값(`signature`, `SignData` 등)은 모두 백엔드가 처리하여 응답으로 내려줍니다. 프론트엔드는 이 응답값을 받아 아래의 각 PG사별 시나리오를 수행하면 됩니다.

---

## 핵심 분기: PG사별 클라이언트 구현 방식의 차이

두 PG사의 클라이언트 구현 방식은 완전히 다릅니다. 이니시스는 **리디렉션 방식**을, 나이스페이는 **콜백 함수와 DOM 조작 방식**을 사용합니다.

### 1. 이니시스(Inicis) 연동 방식: 리디렉션(Redirection)

이니시스는 SDK 함수를 호출하면, 결제 프로세스 완료 후 지정된 `returnUrl`로 페이지 전체를 이동시키는 표준적인 방식을 사용합니다.

**구현 순서 (의사코드)**

```javascript
// (팝업창 내부: /payment-popup 페이지)

// 1. 부모창으로부터 이니시스 파라미터 수신
window.addEventListener("message", (event) => {
  if (event.data.type === "INICIS_PARAMS") {
    const params = event.data.data;

    // 2. form에 값 채우기
    const form = document.getElementById("inicisForm");
    for (const key in params) { form.elements[key].value = params[key]; }

    // 3. 이니시스 SDK 호출 -> 결제창 생성
    INIStdPay.pay("inicisForm");
    // 이 함수 호출 후, 이니시스가 모든 제어권을 가져가며
    // 완료 시 returnUrl로 페이지를 완전히 이동시킴.
  }
});
```

-   **결과 처리**: 결제 완료 후, 이니시스는 `returnUrl`로 지정된 페이지(예: `/order/progress-popup`)로 사용자를 **POST 방식 리디렉션**시킵니다. 따라서, 결과를 처리하는 로직은 `progress-popup` 페이지의 **서버 사이드 코드**에서 시작되어야 합니다. 서버는 POST body에 담겨온 `authToken` 등의 결과 데이터를 읽어 부모창으로 전달하는 스크립트를 렌더링합니다.

### 2. 나이스페이(NicePay) 연동 방식: 콜백(Callback) 및 DOM 조작

나이스페이는 SDK 함수를 호출하면, **페이지를 이동시키지 않고** 결제 프로세스 완료 후 **미리 약속된 이름의 전역 콜백 함수를 호출**합니다. 이때 SDK는 결과값을 **현재 페이지의 DOM(HTML)에 동적으로 추가**합니다.

**구현 순서 (의사코드)**

```javascript
// (팝업창 내부: /payment-popup 페이지)

// 1. **(필수)** SDK가 호출할 전역 콜백 함수를 미리 정의해야 함
window.nicepaySubmit = function() {
  // 4. SDK가 이 함수를 호출하면, DOM에서 결과값을 직접 읽어옴
  const getResult = (name) => document.getElementsByName(name)[0]?.value || null;

  const resultData = {
    success: getResult('AuthResultCode') === '0000',
    AuthResultCode: getResult('AuthResultCode'),
    AuthResultMsg: getResult('AuthResultMsg'),
    TxTid: getResult('TxTid'),
    AuthToken: getResult('AuthToken'),
    // ... 기타 필요한 결과값들
  };

  // 5. 읽어온 결과값을 부모창으로 전송
  window.opener.postMessage({ type: "PAYMENT_RESULT", data: resultData }, "*");
  window.close(); // 스스로 팝업창 닫기
};

// 2. 부모창으로부터 나이스페이 파라미터 수신
window.addEventListener("message", (event) => {
  if (event.data.type === "NICEPAY_PARAMS") {
    const params = event.data.data;
    const form = document.nicePayForm;
    for (const key in params) { form.elements[key].value = params[key]; }

    // 3. 나이스페이 SDK 호출 -> 결제창 생성
    goPay(form);
    // 이 함수는 페이지를 이동시키지 않고, 완료 후 window.nicepaySubmit()을 호출함.
  }
});
```

-   **핵심 로직**: `goPay()` 함수를 호출하기 전에, 반드시 `window.nicepaySubmit` 이라는 이름의 전역 함수를 정의해 두어야 합니다. `goPay` 함수는 결제가 끝나면 이 콜백 함수를 호출하고, 콜백 함수 안에서는 `document.getElementsByName()` 등을 통해 **SDK가 동적으로 생성한 input 태그의 값**을 직접 읽어와야 합니다.

---

## 구현 패턴 요약 및 비교

| 구분 | 이니시스 (Inicis) | 나이스페이 (NicePay) | 비고 |
| :--- | :--- | :--- | :--- |
| **SDK 호출** | `INIStdPay.pay('formId')` | `goPay(formElement)` | 인자의 타입이 다름. |
| **결과 반환 방식** | **페이지 리디렉션** | **전역 콜백 함수 호출** | **가장 결정적인 차이점.** 이니시스는 페이지를 이동시키지만, 나이스페이는 현재 페이지에서 JS 함수를 호출함. |
| **결과 값 위치** | 리디렉션된 페이지의 **POST Body** | 현재 페이지의 **DOM** (hidden input) | 나이스페이의 경우, 콜백 함수 내에서 DOM을 직접 파싱하여 결과 값을 추출해야 함. |

## 프론트엔드 개발자를 위한 최종 권장사항

1.  **PG별 분기 처리**: 팝업 페이지(`payment-popup`)는 `postMessage`로 받은 데이터에 따라 이니시스 로직과 나이스페이 로직(콜백 함수 정의 포함)을 선택적으로 실행하도록 구현해야 합니다.

2.  **나이스페이 콜백 주의**: `window.nicepaySubmit` 함수는 반드시 `goPay`가 호출되기 전에 window 객체에 할당되어 있어야 합니다. 컴포넌트 생명주기(lifecycle)의 가장 빠른 시점에 정의하는 것이 안전합니다.

3.  **이니시스 리턴 처리**: 이니시스의 `returnUrl`이 받는 페이지(`progress-popup`)는 서버 사이드에서 POST 데이터를 처리하여 클라이언트로 내려주는 로직이 반드시 포함되어야 합니다. SPA(Single Page Application) 환경에서는 이 부분을 처리하기 위한 별도의 서버리스 함수나 간단한 백엔드 엔드포인트가 필요할 수 있습니다.