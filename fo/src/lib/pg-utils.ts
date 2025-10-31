import type {
  PgType,
  InicisAuthResponse,
  NiceAuthResponse,
} from '@/types/order.types';

/**
 * PG 팝업 설정
 */
interface PgPopupConfig {
  width: number;
  height: number;
  title: string;
}

const PG_POPUP_CONFIGS: Record<PgType, PgPopupConfig> = {
  INICIS: { width: 840, height: 600, title: 'KG이니시스 결제' },
  NICE: { width: 570, height: 830, title: '나이스페이 결제' },
};

/**
 * PG 팝업 열기
 */
export function openPgPopup(pgType: PgType, url: string): Window | null {
  const config = PG_POPUP_CONFIGS[pgType];
  const left = (window.screen.width - config.width) / 2;
  const top = (window.screen.height - config.height) / 2;

  const features = [
    `width=${config.width}`,
    `height=${config.height}`,
    `left=${left}`,
    `top=${top}`,
    'toolbar=no',
    'menubar=no',
    'scrollbars=yes',
    'resizable=yes',
  ].join(',');

  return window.open(url, config.title, features);
}

/**
 * Inicis 결제 호출
 */
export function callInicisPay(formId: string): void {
  if (typeof window === 'undefined') {
    throw new Error('Window object is not available');
  }

  if (!window.INIStdPay) {
    throw new Error('INIStdPay is not loaded');
  }

  try {
    window.INIStdPay.pay(formId);
  } catch (error) {
    console.error('Inicis payment call failed:', error);
    throw new Error('결제 요청에 실패했습니다. 다시 시도해주세요.');
  }
}

/**
 * Nice 결제 호출
 */
export function callNicePay(form: HTMLFormElement): void {
  if (typeof window === 'undefined') {
    throw new Error('Window object is not available');
  }

  if (!window.goPay) {
    throw new Error('goPay is not loaded');
  }

  try {
    window.goPay(form);
  } catch (error) {
    console.error('Nice payment call failed:', error);
    throw new Error('결제 요청에 실패했습니다. 다시 시도해주세요.');
  }
}

/**
 * Inicis 응답 데이터 추출 (body에서)
 */
export function extractInicisPgData(): InicisAuthResponse | null {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return null;
  }

  try {
    const bodyText = document.body.textContent || '';
    const jsonMatch = bodyText.match(/\{[\s\S]*\}/);

    if (!jsonMatch) {
      return null;
    }

    const data = JSON.parse(jsonMatch[0]) as InicisAuthResponse;
    return data;
  } catch (error) {
    console.error('Failed to extract Inicis data:', error);
    return null;
  }
}

/**
 * Nice 응답 데이터 추출 (form에서)
 */
export function extractNicePgData(): NiceAuthResponse | null {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return null;
  }

  try {
    const form = document.querySelector('form[name="nicePayReturnForm"]') as HTMLFormElement | null;

    if (!form) {
      return null;
    }

    const formData: Record<string, string> = {};
    const inputs = form.querySelectorAll('input');

    inputs.forEach((input) => {
      if (input.name) {
        formData[input.name] = input.value;
      }
    });

    return formData as unknown as NiceAuthResponse;
  } catch (error) {
    console.error('Failed to extract Nice data:', error);
    return null;
  }
}

/**
 * PG 타입에 따라 응답 데이터 추출
 */
export function extractPgData(
  pgType: PgType
): InicisAuthResponse | NiceAuthResponse | null {
  switch (pgType) {
    case 'INICIS':
      return extractInicisPgData();
    case 'NICE':
      return extractNicePgData();
    default:
      return null;
  }
}

/**
 * 결제 성공 여부 확인
 */
export function isPaymentSuccess(
  pgType: PgType,
  authData: InicisAuthResponse | NiceAuthResponse
): boolean {
  if (pgType === 'INICIS') {
    const data = authData as InicisAuthResponse;
    return data.resultCode === '0000';
  } else {
    const data = authData as NiceAuthResponse;
    return data.AuthResultCode === '0000';
  }
}

/**
 * PG 에러 메시지 추출
 */
export function getPgErrorMessage(
  pgType: PgType,
  authData: InicisAuthResponse | NiceAuthResponse
): string {
  if (pgType === 'INICIS') {
    const data = authData as InicisAuthResponse;
    return data.resultMsg || '결제에 실패했습니다.';
  } else {
    const data = authData as NiceAuthResponse;
    return data.AuthResultMsg || '결제에 실패했습니다.';
  }
}
