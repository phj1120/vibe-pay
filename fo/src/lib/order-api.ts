import { apiClient } from "./api-client";
import { OrderSheet } from "@/types/order";
import type {
  OrderNumberResponse,
  PaymentInitiateResponse,
  OrderCreateRequest,
  OrderCreateResponse,
  PaymentMethod,
  PgType,
} from '@/types/order.types';

/**
 * 주문서 정보 조회
 *
 * @param basketNos 장바구니 번호 목록
 * @returns 주문서 정보
 */
export async function getOrderSheet(basketNos: string[]): Promise<OrderSheet> {
  const params = new URLSearchParams();
  basketNos.forEach((no) => params.append("basketNos", no));

  return apiClient<OrderSheet>(`/api/order/sheet?${params.toString()}`);
}

/**
 * 주문번호 생성 API
 */
export async function generateOrderNumber(): Promise<string> {
  const response = await apiClient<OrderNumberResponse>('/api/order/generateOrderNumber');
  return response.orderNumber;
}

/**
 * 결제 초기화 요청 (백엔드 API 스펙에 맞춤)
 */
export interface PaymentInitiateRequestAPI {
  orderNumber: string;
  amount: number;
  productName: string;
  buyerName: string;
  buyerEmail: string;
  buyerTel: string;
}

/**
 * 결제 초기화 API
 */
export async function initiatePayment(
  request: PaymentInitiateRequestAPI
): Promise<PaymentInitiateResponse> {
  const response = await apiClient<PaymentInitiateResponse>('/api/payments/initiate', {
    method: 'POST',
    body: JSON.stringify(request),
  });
  return response;
}

/**
 * 주문 생성 API
 */
export async function createOrder(request: OrderCreateRequest): Promise<OrderCreateResponse> {
  const response = await apiClient<OrderCreateResponse>('/api/order/order', {
    method: 'POST',
    body: JSON.stringify(request),
  });
  return response;
}

/**
 * 주문 완료 정보 조회 API
 */
export interface OrderCompleteGoods {
  goodsNo: string;
  itemNo: string;
  goodsName: string;
  itemName: string;
  salePrice: number;
  quantity: number;
  subtotal: number;
}

export interface OrderCompletePayment {
  payWayCode: string;
  payWayName: string;
  amount: number;
  pgTypeCode?: string;
  pgTypeName?: string;
}

export interface OrderCompleteResponse {
  orderNo: string;
  memberNo: string;
  orderAcceptDtm: string;
  totalAmount: number;
  goodsList: OrderCompleteGoods[];
  paymentList: OrderCompletePayment[];
}

export async function getOrderComplete(orderNo: string): Promise<OrderCompleteResponse> {
  return apiClient<OrderCompleteResponse>(`/api/order/complete/${orderNo}`);
}
