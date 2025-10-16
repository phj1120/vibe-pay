import apiClient from './client';
import { ApiResponse, PageRequest, PageResponse } from '@/types/api';
import { Product, CreateProductRequest, UpdateProductRequest, ProductSearchCondition } from '@/types/product';

// 상품 목록 조회
export const getProducts = async (
  params?: PageRequest & ProductSearchCondition
): Promise<PageResponse<Product>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Product>>>('/products', { params });
  return response.data.data;
};

// 상품 상세 조회
export const getProduct = async (productId: number): Promise<Product> => {
  const response = await apiClient.get<ApiResponse<Product>>(`/products/${productId}`);
  return response.data.data;
};

// 상품 생성
export const createProduct = async (productData: CreateProductRequest): Promise<Product> => {
  const response = await apiClient.post<ApiResponse<Product>>('/products', productData);
  return response.data.data;
};

// 상품 수정
export const updateProduct = async (
  productId: number,
  productData: UpdateProductRequest
): Promise<Product> => {
  const response = await apiClient.put<ApiResponse<Product>>(`/products/${productId}`, productData);
  return response.data.data;
};

// 상품 삭제
export const deleteProduct = async (productId: number): Promise<void> => {
  await apiClient.delete(`/products/${productId}`);
};

// 상품 검색
export const searchProducts = async (
  searchCondition: ProductSearchCondition,
  pageRequest?: PageRequest
): Promise<PageResponse<Product>> => {
  const params = { ...searchCondition, ...pageRequest };
  const response = await apiClient.get<ApiResponse<PageResponse<Product>>>('/products/search', { params });
  return response.data.data;
};