import { apiClient } from "./api-client";
import type {
  GoodsRegisterRequest,
  GoodsModifyRequest,
  GoodsSearchRequest,
  GoodsPageResponse,
  GoodsDetailResponse,
} from "@/types/goods";

/**
 * 상품 등록
 */
export async function registerGoods(
  data: GoodsRegisterRequest
): Promise<string> {
  return apiClient<string>("/api/goods", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

/**
 * 상품 수정
 */
export async function modifyGoods(
  goodsNo: string,
  data: GoodsModifyRequest
): Promise<void> {
  return apiClient<void>(`/api/goods/${goodsNo}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

/**
 * 상품 목록 조회 (페이징)
 */
export async function getGoodsList(
  params: GoodsSearchRequest
): Promise<GoodsPageResponse> {
  const searchParams = new URLSearchParams();

  if (params.goodsStatusCode) {
    searchParams.append("goodsStatusCode", params.goodsStatusCode);
  }
  if (params.goodsName) {
    searchParams.append("goodsName", params.goodsName);
  }
  if (params.page !== undefined) {
    searchParams.append("page", params.page.toString());
  }
  if (params.size !== undefined) {
    searchParams.append("size", params.size.toString());
  }

  const query = searchParams.toString();
  const url = query ? `/api/goods?${query}` : "/api/goods";

  return apiClient<GoodsPageResponse>(url);
}

/**
 * 상품 상세 조회
 */
export async function getGoodsDetail(
  goodsNo: string
): Promise<GoodsDetailResponse> {
  return apiClient<GoodsDetailResponse>(`/api/goods/${goodsNo}`);
}

/**
 * 상품 삭제
 */
export async function deleteGoods(goodsNo: string): Promise<void> {
  return apiClient<void>(`/api/goods/${goodsNo}`, {
    method: "DELETE",
  });
}
