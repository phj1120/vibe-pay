import { apiClient } from "./api-client";
import type {
  PointTransactionRequest,
  PointBalanceResponse,
  PointHistoryListResponse,
} from "@/types/point";

export const pointApi = {
  // 포인트 충전/사용
  processTransaction: async (data: PointTransactionRequest): Promise<void> => {
    await apiClient<void>("/api/point/transaction", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // 보유 포인트 조회
  getBalance: async (): Promise<PointBalanceResponse> => {
    return await apiClient<PointBalanceResponse>("/api/point/balance", {
      method: "GET",
    });
  },

  // 포인트 내역 조회
  getHistory: async (page: number = 0, size: number = 10): Promise<PointHistoryListResponse> => {
    return await apiClient<PointHistoryListResponse>(
      `/api/point/history?page=${page}&size=${size}`,
      {
        method: "GET",
      }
    );
  },
};

// Named exports for convenience
export async function getPointBalance(): Promise<PointBalanceResponse> {
  return pointApi.getBalance();
}
