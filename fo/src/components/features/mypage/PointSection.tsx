"use client";

import { useState } from "react";
import { pointApi } from "@/lib/point-api";
import { ApiError } from "@/lib/api-client";
import type {
  PointBalanceResponse,
  PointHistoryListResponse,
} from "@/types/point";

interface PointSectionProps {
  pointBalance: PointBalanceResponse | null;
  pointHistory: PointHistoryListResponse | null;
  onRefresh: () => void;
}

export default function PointSection({
  pointBalance,
  pointHistory,
  onRefresh,
}: PointSectionProps) {
  const [chargeAmount, setChargeAmount] = useState<string>("");
  const [isCharging, setIsCharging] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);

  const handleQuickCharge = async (amount: number) => {
    setIsCharging(true);
    try {
      await pointApi.processTransaction({
        amount,
        pointTransactionCode: "001",
        pointTransactionReasonCode: "001",
      });

      alert(`${amount.toLocaleString()}원이 충전되었습니다`);
      onRefresh();
    } catch (err) {
      if (err instanceof ApiError) {
        alert(err.message);
      } else {
        alert("포인트 충전 중 오류가 발생했습니다");
      }
    } finally {
      setIsCharging(false);
    }
  };

  const handleCustomCharge = async () => {
    const amount = parseInt(chargeAmount);
    if (isNaN(amount) || amount <= 0) {
      alert("올바른 금액을 입력해주세요");
      return;
    }

    setIsCharging(true);
    try {
      await pointApi.processTransaction({
        amount,
        pointTransactionCode: "001",
        pointTransactionReasonCode: "001",
      });

      setChargeAmount("");
      alert(`${amount.toLocaleString()}원이 충전되었습니다`);
      onRefresh();
    } catch (err) {
      if (err instanceof ApiError) {
        alert(err.message);
      } else {
        alert("포인트 충전 중 오류가 발생했습니다");
      }
    } finally {
      setIsCharging(false);
    }
  };

  const handlePageChange = async (newPage: number) => {
    try {
      const historyData = await pointApi.getHistory(newPage, 10);
      setCurrentPage(newPage);
      onRefresh();
    } catch (err) {
      if (err instanceof ApiError) {
        alert(err.message);
      } else {
        alert("포인트 내역을 불러오는 중 오류가 발생했습니다");
      }
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getTransactionTypeName = (code: string) => {
    return code === "001" ? "적립" : "사용";
  };

  const getReasonName = (code: string) => {
    return code === "001" ? "기타" : "주문";
  };

  return (
    <div>
      {/* 보유 포인트 */}
      {pointBalance && (
        <div className="bg-gray-50 p-8 mb-12">
          <div className="text-sm text-gray-600 mb-2">보유 포인트</div>
          <div className="text-3xl font-medium">
            {pointBalance.totalPoint.toLocaleString()}원
          </div>
        </div>
      )}

      {/* 포인트 충전 */}
      <div className="mb-12">
        <h3 className="text-base font-medium mb-6">충전</h3>

        {/* 정액 충전 버튼 */}
        <div className="grid grid-cols-4 gap-2 mb-4">
          <button
            onClick={() => handleQuickCharge(1000)}
            disabled={isCharging}
            className="py-3 border border-gray-300 hover:border-black text-sm disabled:opacity-30 disabled:cursor-not-allowed transition"
          >
            1,000원
          </button>
          <button
            onClick={() => handleQuickCharge(5000)}
            disabled={isCharging}
            className="py-3 border border-gray-300 hover:border-black text-sm disabled:opacity-30 disabled:cursor-not-allowed transition"
          >
            5,000원
          </button>
          <button
            onClick={() => handleQuickCharge(10000)}
            disabled={isCharging}
            className="py-3 border border-gray-300 hover:border-black text-sm disabled:opacity-30 disabled:cursor-not-allowed transition"
          >
            10,000원
          </button>
          <button
            onClick={() => handleQuickCharge(50000)}
            disabled={isCharging}
            className="py-3 border border-gray-300 hover:border-black text-sm disabled:opacity-30 disabled:cursor-not-allowed transition"
          >
            50,000원
          </button>
        </div>

        {/* 직접 입력 */}
        <div className="flex gap-2">
          <input
            type="number"
            value={chargeAmount}
            onChange={(e) => setChargeAmount(e.target.value)}
            placeholder="금액 입력"
            disabled={isCharging}
            className="flex-1 px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm disabled:opacity-50"
          />
          <button
            onClick={handleCustomCharge}
            disabled={isCharging}
            className="px-8 py-3 bg-black text-white text-sm hover:bg-gray-800 disabled:bg-gray-300 disabled:cursor-not-allowed"
          >
            {isCharging ? "충전 중..." : "충전"}
          </button>
        </div>
      </div>

      {/* 포인트 내역 */}
      <div>
        <h3 className="text-base font-medium mb-6">내역</h3>

        {pointHistory && pointHistory.content.length > 0 ? (
          <>
            <div className="space-y-4">
              {pointHistory.content.map((history) => (
                <div
                  key={history.pointHistoryNo}
                  className="flex justify-between items-center py-4 border-b"
                >
                  <div>
                    <div className="text-sm mb-1">
                      {getTransactionTypeName(history.pointTransactionCode)}
                    </div>
                    <div className="text-xs text-gray-500">
                      {formatDate(history.createdDate)}
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-medium">
                      {history.pointTransactionCode === "001" ? "+" : "-"}
                      {history.amount.toLocaleString()}원
                    </div>
                    {history.remainPoint !== undefined &&
                      history.remainPoint !== null && (
                        <div className="text-xs text-gray-500">
                          잔액 {history.remainPoint.toLocaleString()}원
                        </div>
                      )}
                  </div>
                </div>
              ))}
            </div>

            {/* 페이징 */}
            {pointHistory.totalPages > 1 && (
              <div className="flex justify-center items-center gap-4 mt-8">
                <button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                  className="text-sm disabled:opacity-30 disabled:cursor-not-allowed hover:opacity-60"
                >
                  이전
                </button>
                <div className="text-sm text-gray-600">
                  {currentPage + 1} / {pointHistory.totalPages}
                </div>
                <button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage >= pointHistory.totalPages - 1}
                  className="text-sm disabled:opacity-30 disabled:cursor-not-allowed hover:opacity-60"
                >
                  다음
                </button>
              </div>
            )}
          </>
        ) : (
          <div className="text-center py-12 text-gray-400 text-sm">
            포인트 내역이 없습니다
          </div>
        )}
      </div>
    </div>
  );
}
