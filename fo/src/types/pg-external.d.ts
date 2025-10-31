/**
 * PG 외부 스크립트 타입 선언
 */

declare global {
  interface Window {
    /**
     * KG이니시스 결제 함수
     */
    INIStdPay?: {
      pay: (formId: string) => void;
    };

    /**
     * 나이스페이 결제 함수
     */
    goPay?: (form: HTMLFormElement) => void;
  }
}

export {};
