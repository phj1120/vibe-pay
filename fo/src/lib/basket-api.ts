import { apiClient } from "./api-client";
import type { BasketItem, BasketAddRequest, BasketModifyRequest } from "@/types/basket";

/**
 * 장바구니 목록 조회
 */
export async function getBasketList(): Promise<BasketItem[]> {
  return apiClient<BasketItem[]>("/api/baskets");
}

/**
 * 장바구니 추가
 */
export async function addBasket(data: BasketAddRequest): Promise<string> {
  return apiClient<string>("/api/baskets", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

/**
 * 장바구니 수정
 */
export async function modifyBasket(
  basketNo: string,
  data: BasketModifyRequest
): Promise<void> {
  return apiClient<void>(`/api/baskets/${basketNo}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

/**
 * 장바구니 삭제
 */
export async function deleteBasket(basketNo: string): Promise<void> {
  return apiClient<void>(`/api/baskets/${basketNo}`, {
    method: "DELETE",
  });
}

/**
 * 장바구니 여러 개 삭제
 */
export async function deleteBaskets(basketNos: string[]): Promise<void> {
  const searchParams = new URLSearchParams();
  basketNos.forEach((basketNo) => searchParams.append("basketNos", basketNo));

  return apiClient<void>(`/api/baskets?${searchParams.toString()}`, {
    method: "DELETE",
  });
}

/**
 * 장바구니 전체 삭제
 */
export async function deleteAllBaskets(): Promise<void> {
  return apiClient<void>("/api/baskets/all", {
    method: "DELETE",
  });
}
