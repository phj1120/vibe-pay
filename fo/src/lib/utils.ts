import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

// Tailwind CSS 클래스명 병합 유틸리티
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// 날짜 포맷팅
export function formatDate(date: string | Date, format: 'date' | 'datetime' | 'time' = 'date'): string {
  const d = typeof date === 'string' ? new Date(date) : date;

  if (isNaN(d.getTime())) {
    return '';
  }

  switch (format) {
    case 'date':
      return d.toLocaleDateString('ko-KR');
    case 'datetime':
      return d.toLocaleString('ko-KR');
    case 'time':
      return d.toLocaleTimeString('ko-KR');
    default:
      return d.toLocaleDateString('ko-KR');
  }
}

// 금액 포맷팅
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
  }).format(amount);
}

// 숫자 포맷팅 (천 단위 구분자)
export function formatNumber(num: number): string {
  return new Intl.NumberFormat('ko-KR').format(num);
}

// 전화번호 포맷팅
export function formatPhoneNumber(phoneNumber: string): string {
  const cleaned = phoneNumber.replace(/\D/g, '');

  if (cleaned.length === 11) {
    return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  } else if (cleaned.length === 10) {
    return cleaned.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  }

  return phoneNumber;
}

// 이메일 마스킹
export function maskEmail(email: string): string {
  const [localPart, domain] = email.split('@');
  if (!domain) return email;

  const maskedLocal = localPart.length > 2
    ? localPart.substring(0, 2) + '*'.repeat(localPart.length - 2)
    : localPart;

  return `${maskedLocal}@${domain}`;
}

// 지연 함수 (디바운싱 등에 사용)
export function delay(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// 랜덤 ID 생성
export function generateId(): string {
  return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

// 객체 깊은 복사
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj.getTime()) as unknown as T;
  if (obj instanceof Array) return obj.map(item => deepClone(item)) as unknown as T;
  if (typeof obj === 'object') {
    const clonedObj = {} as T;
    for (const key in obj) {
      clonedObj[key] = deepClone(obj[key]);
    }
    return clonedObj;
  }
  return obj;
}