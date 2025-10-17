// 결제 방법 열거형
export type PaymentMethod = 'CARD' | 'BANK_TRANSFER' | 'POINTS' | 'VIRTUAL_ACCOUNT';

// 결제 타입 열거형
export type PayType = 'AUTH' | 'CANCEL' | 'REFUND';

// PG사 열거형
export type PgCompany = 'INICIS' | 'NICEPAY' | 'TOSS' | 'KAKAO';

// 결제 상태 열거형
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'FAILED' | 'REFUNDED';

// 주문 상태 열거형
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

// 결제 타입
export interface Payment {
  paymentId: string;
  memberId: number;
  orderId: string;
  claimId?: string;
  amount: number;
  paymentMethod: PaymentMethod;
  payType: PayType;
  pgCompany: PgCompany;
  status: PaymentStatus;
  orderStatus: OrderStatus;
  transactionId?: string;
  paymentDate: string;
}

// 결제 요청 타입
export interface PaymentRequest {
  orderId: string;
  paymentMethod: PaymentMethod;
  amount: number;
  pgCompany: PgCompany;
}

// 결제 응답 타입
export interface PaymentResponse {
  paymentId: string;
  transactionId: string;
  status: PaymentStatus;
  amount: number;
  paymentDate: string;
  pgResponse?: {
    resultCode: string;
    resultMessage: string;
  };
}

// 환불 요청 타입
export interface RefundRequest {
  paymentId: string;
  amount: number;
  reason?: string;
}

// 결제 초기화 요청 타입
export interface PaymentInitiateRequest {
  memberId: number;
  amount: number;
  paymentMethod: string;
  pgCompany: string;
  orderId: string;
  productName: string;
  buyerName: string;
  buyerPhone: string;
  buyerEmail: string;
  returnUrl?: string;
  cancelUrl?: string;
}

// 결제 초기화 응답 타입
export interface PaymentInitResponse {
  success: boolean;
  paymentUrl: string;
  parameters: Record<string, string>;
  message?: string;
  errorCode?: string;
}

// 결제 승인 요청 타입 (PG로부터 받은 데이터)
export interface PaymentConfirmRequest {
  orderId: string;
  authToken: string;
  authUrl: string;
  mid?: string;
  amount: number;
  memberId: number;
  paymentMethod: string;
  pgCompany: string;
  netCancelUrl?: string;
}