import apiClient from './client';
import { ApiResponse, PageRequest, PageResponse } from '@/types/api';
import { Order, OrderItem, CreateOrderRequest, CancelOrderRequest } from '@/types/order';

// 주문 목록 조회
export const getOrders = async (params?: PageRequest): Promise<PageResponse<Order>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Order>>>('/orders', { params });
  return response.data.data;
};

// 회원별 주문 목록 조회
export const getMemberOrders = async (
  memberId: number,
  params?: PageRequest
): Promise<PageResponse<Order>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Order>>>(
    `/members/${memberId}/orders`,
    { params }
  );
  return response.data.data;
};

// 주문 상세 조회
export const getOrder = async (orderId: string): Promise<Order> => {
  const response = await apiClient.get<ApiResponse<Order>>(`/orders/${orderId}`);
  return response.data.data;
};

// 주문 생성
export const createOrder = async (orderData: CreateOrderRequest): Promise<Order> => {
  const response = await apiClient.post<ApiResponse<Order>>('/orders', orderData);
  return response.data.data;
};

// 주문 취소
export const cancelOrder = async (cancelData: CancelOrderRequest): Promise<Order> => {
  const response = await apiClient.post<ApiResponse<Order>>(
    `/orders/${cancelData.orderId}/cancel`,
    { reason: cancelData.reason }
  );
  return response.data.data;
};

// 주문 아이템 목록 조회
export const getOrderItems = async (orderId: string): Promise<OrderItem[]> => {
  const response = await apiClient.get<ApiResponse<OrderItem[]>>(`/orders/${orderId}/items`);
  return response.data.data;
};

// 주문 상태별 조회
export const getOrdersByStatus = async (
  status: string,
  params?: PageRequest
): Promise<PageResponse<Order>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Order>>>(
    `/orders/status/${status}`,
    { params }
  );
  return response.data.data;
};