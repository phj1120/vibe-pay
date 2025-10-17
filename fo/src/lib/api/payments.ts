import apiClient from './client';
import { ApiResponse, PageRequest, PageResponse } from '@/types/api';
import {
  Payment,
  PaymentRequest,
  PaymentResponse,
  RefundRequest,
  PaymentInitiateRequest,
  PaymentInitResponse,
  PaymentConfirmRequest
} from '@/types/payment';

// 결제 목록 조회
export const getPayments = async (params?: PageRequest): Promise<PageResponse<Payment>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Payment>>>('/payments', { params });
  return response.data.data;
};

// 회원별 결제 목록 조회
export const getMemberPayments = async (
  memberId: number,
  params?: PageRequest
): Promise<PageResponse<Payment>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Payment>>>(
    `/members/${memberId}/payments`,
    { params }
  );
  return response.data.data;
};

// 주문별 결제 목록 조회
export const getOrderPayments = async (orderId: string): Promise<Payment[]> => {
  const response = await apiClient.get<ApiResponse<Payment[]>>(`/orders/${orderId}/payments`);
  return response.data.data;
};

// 결제 상세 조회
export const getPayment = async (paymentId: string): Promise<Payment> => {
  const response = await apiClient.get<ApiResponse<Payment>>(`/payments/${paymentId}`);
  return response.data.data;
};

// 결제 처리
export const processPayment = async (paymentData: PaymentRequest): Promise<PaymentResponse> => {
  const response = await apiClient.post<ApiResponse<PaymentResponse>>('/payments/process', paymentData);
  return response.data.data;
};

// 결제 승인
export const confirmPayment = async (paymentId: string): Promise<PaymentResponse> => {
  const response = await apiClient.post<ApiResponse<PaymentResponse>>(`/payments/${paymentId}/confirm`);
  return response.data.data;
};

// 결제 환불
export const refundPayment = async (refundData: RefundRequest): Promise<PaymentResponse> => {
  const response = await apiClient.post<ApiResponse<PaymentResponse>>(
    `/payments/${refundData.paymentId}/refund`,
    { amount: refundData.amount, reason: refundData.reason }
  );
  return response.data.data;
};

// 결제 취소
export const cancelPayment = async (paymentId: string, reason?: string): Promise<PaymentResponse> => {
  const response = await apiClient.post<ApiResponse<PaymentResponse>>(
    `/payments/${paymentId}/cancel`,
    { reason }
  );
  return response.data.data;
};

// 결제 초기화 (PG 결제창 파라미터 받기)
export const initiatePayment = async (
  request: PaymentInitiateRequest
): Promise<PaymentInitResponse> => {
  const response = await apiClient.post<ApiResponse<PaymentInitResponse>>(
    '/api/payments/initiate',
    request
  );
  return response.data.data;
};

// 결제 승인 (PG 리다이렉트 후 최종 승인)
export const confirmPaymentWithPG = async (
  request: PaymentConfirmRequest
): Promise<PaymentResponse> => {
  const response = await apiClient.post<ApiResponse<PaymentResponse>>(
    '/api/payments/confirm',
    request
  );
  return response.data.data;
};