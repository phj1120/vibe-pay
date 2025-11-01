import { z } from 'zod';

/**
 * 결제수단 ENUM
 */
export const PaymentMethod = {
  CARD: 'CARD',
  VIRTUAL_ACCOUNT: 'VIRTUAL_ACCOUNT',
  ACCOUNT_TRANSFER: 'ACCOUNT_TRANSFER',
  PHONE: 'PHONE',
} as const;

export type PaymentMethod = (typeof PaymentMethod)[keyof typeof PaymentMethod];

/**
 * PG사 ENUM
 */
export const PgType = {
  INICIS: 'INICIS',
  NICE: 'NICE',
} as const;

export type PgType = (typeof PgType)[keyof typeof PgType];

/**
 * 주문 상태 ENUM
 */
export const OrderStatus = {
  PENDING: 'PENDING',
  PAYMENT_PROCESSING: 'PAYMENT_PROCESSING',
  PAYMENT_COMPLETED: 'PAYMENT_COMPLETED',
  PAYMENT_FAILED: 'PAYMENT_FAILED',
  CANCELLED: 'CANCELLED',
} as const;

export type OrderStatus = (typeof OrderStatus)[keyof typeof OrderStatus];

/**
 * 주문 상품 정보
 */
export const OrderProductSchema = z.object({
  goodsNo: z.string(),
  goodsName: z.string(),
  price: z.number(),
  quantity: z.number(),
  totalPrice: z.number(),
});

export type OrderProduct = z.infer<typeof OrderProductSchema>;

/**
 * 주문자 정보
 */
export const OrdererInfoSchema = z.object({
  name: z.string().min(1, '주문자명을 입력해주세요'),
  phone: z.string().regex(/^010-\d{4}-\d{4}$/, '올바른 전화번호 형식이 아닙니다 (010-0000-0000)'),
  email: z.string().email('올바른 이메일 형식이 아닙니다'),
});

export type OrdererInfo = z.infer<typeof OrdererInfoSchema>;

/**
 * 배송 정보
 */
export const DeliveryInfoSchema = z.object({
  recipientName: z.string().min(1, '수령자명을 입력해주세요'),
  phone: z.string().regex(/^010-\d{4}-\d{4}$/, '올바른 전화번호 형식이 아닙니다 (010-0000-0000)'),
  zipCode: z.string().min(1, '우편번호를 입력해주세요'),
  address: z.string().min(1, '주소를 입력해주세요'),
  addressDetail: z.string().optional(),
  deliveryMessage: z.string().optional(),
});

export type DeliveryInfo = z.infer<typeof DeliveryInfoSchema>;

/**
 * 결제 정보
 */
export const PaymentInfoSchema = z.object({
  paymentMethod: z.enum([
    PaymentMethod.CARD,
    PaymentMethod.VIRTUAL_ACCOUNT,
    PaymentMethod.ACCOUNT_TRANSFER,
    PaymentMethod.PHONE,
  ]),
  pgType: z.enum([PgType.INICIS, PgType.NICE]),
});

export type PaymentInfo = z.infer<typeof PaymentInfoSchema>;

/**
 * 주문서 요청 데이터
 */
export const OrderSheetRequestSchema = z.object({
  ordererInfo: OrdererInfoSchema,
  deliveryInfo: DeliveryInfoSchema,
  paymentInfo: PaymentInfoSchema,
  products: z.array(OrderProductSchema).min(1, '상품을 선택해주세요'),
  totalAmount: z.number().min(0),
  discountAmount: z.number().min(0).default(0),
  deliveryFee: z.number().min(0).default(0),
  finalAmount: z.number().min(0),
});

export type OrderSheetRequest = z.infer<typeof OrderSheetRequestSchema>;

/**
 * 결제 초기화 응답
 */
export interface PaymentInitiateResponse {
  pgType: PgType;
  paymentMethod: PaymentMethod;
  merchantId: string;
  merchantKey: string;
  returnUrl: string;
  formData: Record<string, string>;
}

/**
 * PG 인증 응답 (Inicis)
 */
export interface InicisAuthResponse {
  // 결제 결과 코드 (PG사 판별 키 필드)
  resultCode: string;          // '0000'이면 성공
  resultMsg?: string;          // 결과 메시지
  
  // 결제 정보
  mid?: string;                // 상점 ID
  orderNumber?: string;        // 주문번호
  authToken?: string;          // 승인 토큰
  authUrl?: string;            // 승인 URL
  netCancelUrl?: string;       // 망취소 URL
  charset?: string;            // 문자셋
  merchantData?: string;       // 가맹점 데이터
  
  // 동적 필드를 위한 인덱스 시그니처
  [key: string]: string | undefined;
}

/**
 * PG 인증 응답 (Nice)
 */
export interface NiceAuthResponse {
  // 결제 결과 코드 (PG사 판별 키 필드)
  AuthResultCode?: string;     // '0000'이면 성공
  AuthResultMsg?: string;       // 결과 메시지

  // 결제 정보
  PayMethod: string;            // 결제 수단
  MID: string;                  // 가맹점 ID
  Moid: string;                 // 주문번호
  Amt: string;                  // 금액
  TxTid?: string;               // 거래번호
  Signature?: string;           // 서명값 (transactionId)
  AuthToken?: string;           // 인증 토큰
  NextAppURL?: string;          // 승인 URL (authUrl)
  NetCancelURL?: string;        // 망취소 URL

  // 동적 필드를 위한 인덱스 시그니처
  [key: string]: string | undefined;
}

/**
 * 주문 생성 요청 (백엔드 API 스펙에 맞춤)
 */
export interface OrderCreateRequest {
  orderNo: string; // 프론트에서 생성한 주문번호
  memberName: string;
  phone: string;
  email: string;
  goodsList: any[]; // BasketResponse 타입
  payList: any[]; // PayRequest 타입
}

/**
 * 주문 생성 응답
 */
export interface OrderCreateResponse {
  orderId: number;
  orderNumber: string;
  status: OrderStatus;
  createdAt: string;
}

/**
 * 주문 번호 생성 응답
 */
export interface OrderNumberResponse {
  orderNumber: string;
}

/**
 * 쿠키에 저장할 주문 데이터
 */
export interface OrderCookieData {
  orderNumber: string;
  orderInfo: Omit<OrderSheetRequest, 'products'> & {
    products: OrderProduct[];
  };
  paymentInitiate: PaymentInitiateResponse;
  timestamp: number;
}

/**
 * 결제 결과 메시지
 */
export interface PaymentResultMessage {
  success: boolean;
  authData?: InicisAuthResponse | NiceAuthResponse;
  error?: string;
  errorDetails?: {
    pgType?: string;
    errorCode?: string;
    errorMessage?: string;
    timestamp?: string;
  };
}
