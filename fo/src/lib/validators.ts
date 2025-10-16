// 이메일 유효성 검사
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// 전화번호 유효성 검사 (한국 형식)
export const validatePhoneNumber = (phoneNumber: string): boolean => {
  const cleaned = phoneNumber.replace(/\D/g, '');
  // 휴대폰: 010,011,016,017,018,019 + 8자리
  // 일반전화: 지역번호 + 7~8자리
  const phoneRegex = /^(01[016789]|02|0[3-9][0-9])\d{7,8}$/;
  return phoneRegex.test(cleaned);
};

// 비밀번호 유효성 검사
export const validatePassword = (password: string): {
  isValid: boolean;
  errors: string[];
} => {
  const errors: string[] = [];

  if (password.length < 8) {
    errors.push('비밀번호는 8자 이상이어야 합니다.');
  }

  if (!/[a-z]/.test(password)) {
    errors.push('소문자를 포함해야 합니다.');
  }

  if (!/[A-Z]/.test(password)) {
    errors.push('대문자를 포함해야 합니다.');
  }

  if (!/\d/.test(password)) {
    errors.push('숫자를 포함해야 합니다.');
  }

  if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    errors.push('특수문자를 포함해야 합니다.');
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
};

// 이름 유효성 검사 (한글, 영문)
export const validateName = (name: string): boolean => {
  const nameRegex = /^[가-힣a-zA-Z\s]{2,20}$/;
  return nameRegex.test(name.trim());
};

// 금액 유효성 검사
export const validateAmount = (amount: number | string): boolean => {
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  return !isNaN(num) && num > 0 && num <= 10000000; // 1천만원 이하
};

// 수량 유효성 검사
export const validateQuantity = (quantity: number | string): boolean => {
  const num = typeof quantity === 'string' ? parseInt(quantity) : quantity;
  return Number.isInteger(num) && num > 0 && num <= 999;
};

// 카드번호 유효성 검사 (Luhn 알고리즘)
export const validateCardNumber = (cardNumber: string): boolean => {
  const cleaned = cardNumber.replace(/\D/g, '');

  if (cleaned.length < 13 || cleaned.length > 19) {
    return false;
  }

  // Luhn 알고리즘
  let sum = 0;
  let shouldDouble = false;

  for (let i = cleaned.length - 1; i >= 0; i--) {
    let digit = parseInt(cleaned.charAt(i));

    if (shouldDouble) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }

    sum += digit;
    shouldDouble = !shouldDouble;
  }

  return sum % 10 === 0;
};

// CVV 유효성 검사
export const validateCVV = (cvv: string): boolean => {
  const cleaned = cvv.replace(/\D/g, '');
  return cleaned.length === 3 || cleaned.length === 4;
};

// 만료일 유효성 검사 (MM/YY)
export const validateExpiryDate = (expiryDate: string): boolean => {
  const regex = /^(0[1-9]|1[0-2])\/\d{2}$/;
  if (!regex.test(expiryDate)) {
    return false;
  }

  const [month, year] = expiryDate.split('/');
  const currentDate = new Date();
  const currentYear = currentDate.getFullYear() % 100;
  const currentMonth = currentDate.getMonth() + 1;

  const expYear = parseInt(year);
  const expMonth = parseInt(month);

  if (expYear < currentYear || (expYear === currentYear && expMonth < currentMonth)) {
    return false;
  }

  return true;
};

// 주소 유효성 검사
export const validateAddress = (address: string): boolean => {
  return address.trim().length >= 5 && address.trim().length <= 200;
};

// 우편번호 유효성 검사 (한국 5자리)
export const validateZipCode = (zipCode: string): boolean => {
  const zipRegex = /^\d{5}$/;
  return zipRegex.test(zipCode);
};

// 필수 필드 검사
export const validateRequired = (value: string | number | null | undefined): boolean => {
  if (value === null || value === undefined) return false;
  if (typeof value === 'string') return value.trim().length > 0;
  return true;
};

// 문자열 길이 범위 검사
export const validateLength = (
  value: string,
  min: number = 0,
  max: number = Number.MAX_SAFE_INTEGER
): boolean => {
  const length = value.trim().length;
  return length >= min && length <= max;
};

// URL 유효성 검사
export const validateURL = (url: string): boolean => {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
};