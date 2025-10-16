// 금액 포맷터
export const formatCurrency = (amount: number, showSymbol: boolean = true): string => {
  const formatted = new Intl.NumberFormat('ko-KR').format(amount);
  return showSymbol ? `₩${formatted}` : formatted;
};

// 날짜 포맷터
export const formatDate = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};

// 날짜시간 포맷터
export const formatDateTime = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
};

// 시간 포맷터
export const formatTime = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

// 전화번호 포맷터
export const formatPhoneNumber = (phoneNumber: string): string => {
  const cleaned = phoneNumber.replace(/\D/g, '');

  if (cleaned.length === 11) {
    return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  } else if (cleaned.length === 10) {
    return cleaned.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  } else if (cleaned.length === 8) {
    return cleaned.replace(/(\d{4})(\d{4})/, '$1-$2');
  }

  return phoneNumber;
};

// 카드번호 포맷터 (마스킹)
export const formatCardNumber = (cardNumber: string, mask: boolean = true): string => {
  const cleaned = cardNumber.replace(/\D/g, '');

  if (mask && cleaned.length >= 8) {
    const first = cleaned.substring(0, 4);
    const last = cleaned.substring(cleaned.length - 4);
    const middle = '*'.repeat(cleaned.length - 8);
    return `${first}${middle}${last}`.replace(/(.{4})/g, '$1-').slice(0, -1);
  }

  return cleaned.replace(/(.{4})/g, '$1-').slice(0, -1);
};

// 주문번호 포맷터
export const formatOrderId = (orderId: string, ordSeq?: number, ordProcSeq?: number): string => {
  let formatted = orderId;

  if (ordSeq !== undefined) {
    formatted += `-${ordSeq.toString().padStart(3, '0')}`;
  }

  if (ordProcSeq !== undefined) {
    formatted += `-${ordProcSeq.toString().padStart(2, '0')}`;
  }

  return formatted;
};

// 파일 크기 포맷터
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';

  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 상대 시간 포맷터 (예: 2시간 전)
export const formatRelativeTime = (date: string | Date): string => {
  const d = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  const diff = now.getTime() - d.getTime();

  const minutes = Math.floor(diff / (1000 * 60));
  const hours = Math.floor(diff / (1000 * 60 * 60));
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (minutes < 1) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;
  if (days < 7) return `${days}일 전`;

  return formatDate(d);
};

// 주소 포맷터
export const formatAddress = (address: string, maxLength: number = 50): string => {
  if (address.length <= maxLength) return address;
  return address.substring(0, maxLength) + '...';
};

// 퍼센트 포맷터
export const formatPercentage = (value: number, decimals: number = 1): string => {
  return `${value.toFixed(decimals)}%`;
};