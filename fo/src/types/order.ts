// 주문 상태 열거형
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

// 주문 타입
export interface Order {
  orderId: string;
  ordSeq: number;
  ordProcSeq: number;
  claimId?: string;
  memberId: number;
  orderDate: string;
  totalAmount: number;
  status: OrderStatus;
  member?: {
    name: string;
    email: string;
  };
  orderItems?: OrderItem[];
}

// 주문 아이템 타입
export interface OrderItem {
  orderItemId: number;
  orderId: string;
  ordSeq: number;
  ordProcSeq: number;
  productId: number;
  quantity: number;
  priceAtOrder: number;
  product?: {
    name: string;
    price: number;
  };
}

// 주문 생성 요청 타입
export interface CreateOrderRequest {
  memberId: number;
  orderItems: {
    productId: number;
    quantity: number;
  }[];
  paymentMethods: {
    paymentMethod: string;
    amount: number;
  }[];
}

// 주문 취소 요청 타입
export interface CancelOrderRequest {
  orderId: string;
  reason?: string;
}