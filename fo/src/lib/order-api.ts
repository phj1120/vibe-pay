import { apiClient } from "./api-client";
import { OrderSheet } from "@/types/order";

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
