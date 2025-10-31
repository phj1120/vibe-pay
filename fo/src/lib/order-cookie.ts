import Cookies from 'js-cookie';
import type { OrderCookieData } from '@/types/order.types';

const ORDER_COOKIE_NAME = 'vibe_order_data';
const COOKIE_EXPIRES_MINUTES = 5;

/**
 * 주문 정보를 쿠키에 저장
 * - 만료시간: 5분
 * - 암호화 없음 (요구사항)
 */
export function setOrderCookie(data: OrderCookieData): void {
  const cookieData = JSON.stringify(data);

  Cookies.set(ORDER_COOKIE_NAME, cookieData, {
    expires: COOKIE_EXPIRES_MINUTES / (24 * 60), // 분을 일로 변환
    path: '/',
    sameSite: 'lax',
  });
}

/**
 * 쿠키에서 주문 정보 읽기
 */
export function getOrderCookie(): OrderCookieData | null {
  const cookieData = Cookies.get(ORDER_COOKIE_NAME);

  if (!cookieData) {
    return null;
  }

  try {
    const data = JSON.parse(cookieData) as OrderCookieData;

    // 타임스탬프 검증 (5분 초과 시 null 반환)
    const now = Date.now();
    const elapsed = now - data.timestamp;
    const maxAge = COOKIE_EXPIRES_MINUTES * 60 * 1000;

    if (elapsed > maxAge) {
      deleteOrderCookie();
      return null;
    }

    return data;
  } catch (error) {
    console.error('Failed to parse order cookie:', error);
    deleteOrderCookie();
    return null;
  }
}

/**
 * 주문 쿠키 삭제
 */
export function deleteOrderCookie(): void {
  Cookies.remove(ORDER_COOKIE_NAME, { path: '/' });
}

/**
 * 쿠키 존재 여부 확인
 */
export function hasOrderCookie(): boolean {
  return Cookies.get(ORDER_COOKIE_NAME) !== undefined;
}
