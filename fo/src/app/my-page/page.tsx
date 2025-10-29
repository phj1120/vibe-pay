"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { memberApi } from "@/lib/member-api";
import { pointApi } from "@/lib/point-api";
import { ApiError } from "@/lib/api-client";
import type { MemberInfoResponse } from "@/types/member";
import type {
  PointBalanceResponse,
  PointHistoryListResponse,
  PointHistoryResponse,
} from "@/types/point";

export default function MyPage() {
  const router = useRouter();
  const [memberInfo, setMemberInfo] = useState<MemberInfoResponse | null>(null);
  const [pointBalance, setPointBalance] = useState<PointBalanceResponse | null>(null);
  const [pointHistory, setPointHistory] = useState<PointHistoryListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [chargeAmount, setChargeAmount] = useState<string>("");
  const [isCharging, setIsCharging] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = localStorage.getItem("accessToken");
        if (!token) {
          router.push("/login");
          return;
        }

        const [memberData, balanceData, historyData] = await Promise.all([
          memberApi.getMyInfo(),
          pointApi.getBalance(),
          pointApi.getHistory(0, 10),
        ]);

        setMemberInfo(memberData);
        setPointBalance(balanceData);
        setPointHistory(historyData);
      } catch (err) {
        if (err instanceof ApiError) {
          if (err.code === "2001" || err.code === "2002" || err.code === "2003") {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            router.push("/login");
            return;
          }
          setError(err.message);
        } else {
          setError("데이터를 불러오는 중 오류가 발생했습니다");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [router]);

  const handleQuickCharge = async (amount: number) => {
    setIsCharging(true);
    try {
      await pointApi.processTransaction({
        amount,
        pointTransactionCode: "001",
        pointTransactionReasonCode: "001",
      });

      const [balanceData, historyData] = await Promise.all([
        pointApi.getBalance(),
        pointApi.getHistory(currentPage, 10),
      ]);

      setPointBalance(balanceData);
      setPointHistory(historyData);
      alert(`${amount.toLocaleString()}원이 충전되었습니다`);
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

      const [balanceData, historyData] = await Promise.all([
        pointApi.getBalance(),
        pointApi.getHistory(currentPage, 10),
      ]);

      setPointBalance(balanceData);
      setPointHistory(historyData);
      setChargeAmount("");
      alert(`${amount.toLocaleString()}원이 충전되었습니다`);
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
      setPointHistory(historyData);
      setCurrentPage(newPage);
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

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    alert("로그아웃 되었습니다");
    router.push("/login");
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="text-gray-600">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-100">
        <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm mb-4">
            {error}
          </div>
          <button
            onClick={() => router.push("/login")}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700"
          >
            로그인 페이지로 이동
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <div className="bg-white rounded-lg shadow-md p-8">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold">마이페이지</h1>
            <button
              onClick={handleLogout}
              className="bg-gray-600 text-white py-2 px-4 rounded-md hover:bg-gray-700 text-sm"
            >
              로그아웃
            </button>
          </div>

          {memberInfo && (
            <div className="space-y-4">
              <div className="border-b pb-4">
                <label className="block text-sm font-medium text-gray-500 mb-1">이름</label>
                <p className="text-lg">{memberInfo.memberName}</p>
              </div>

              <div className="border-b pb-4">
                <label className="block text-sm font-medium text-gray-500 mb-1">이메일</label>
                <p className="text-lg">{memberInfo.email}</p>
              </div>

              <div className="border-b pb-4">
                <label className="block text-sm font-medium text-gray-500 mb-1">전화번호</label>
                <p className="text-lg">{memberInfo.phone}</p>
              </div>

              <div className="border-b pb-4">
                <label className="block text-sm font-medium text-gray-500 mb-1">회원 상태</label>
                <p className="text-lg">
                  {memberInfo.memberStatusCode === "001" ? (
                    <span className="text-green-600">정상</span>
                  ) : (
                    <span className="text-red-600">탈퇴</span>
                  )}
                </p>
              </div>
            </div>
          )}
        </div>

        {/* 포인트 관리 영역 */}
        <div className="bg-white rounded-lg shadow-md p-8 mt-6">
          <h2 className="text-2xl font-bold mb-6">포인트 관리</h2>

          {/* 보유 포인트 */}
          {pointBalance && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
              <div className="text-sm text-gray-600 mb-1">보유 포인트</div>
              <div className="text-3xl font-bold text-blue-600">
                {pointBalance.totalPoint.toLocaleString()}원
              </div>
            </div>
          )}

          {/* 포인트 충전 */}
          <div className="mb-8">
            <h3 className="text-lg font-semibold mb-4">포인트 충전</h3>

            {/* 정액 충전 버튼 */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
              <button
                onClick={() => handleQuickCharge(1000)}
                disabled={isCharging}
                className="py-3 px-4 bg-white border-2 border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                1,000원
              </button>
              <button
                onClick={() => handleQuickCharge(5000)}
                disabled={isCharging}
                className="py-3 px-4 bg-white border-2 border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                5,000원
              </button>
              <button
                onClick={() => handleQuickCharge(10000)}
                disabled={isCharging}
                className="py-3 px-4 bg-white border-2 border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                10,000원
              </button>
              <button
                onClick={() => handleQuickCharge(50000)}
                disabled={isCharging}
                className="py-3 px-4 bg-white border-2 border-gray-300 rounded-lg hover:border-blue-500 hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed transition"
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
                placeholder="충전할 금액을 입력하세요"
                disabled={isCharging}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
              />
              <button
                onClick={handleCustomCharge}
                disabled={isCharging}
                className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
              >
                {isCharging ? "충전 중..." : "충전하기"}
              </button>
            </div>
          </div>

          {/* 포인트 내역 */}
          <div>
            <h3 className="text-lg font-semibold mb-4">포인트 내역</h3>

            {pointHistory && pointHistory.content.length > 0 ? (
              <>
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-gray-50 border-b">
                      <tr>
                        <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">거래일시</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">구분</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">사유</th>
                        <th className="px-4 py-3 text-right text-sm font-medium text-gray-600">금액</th>
                        <th className="px-4 py-3 text-right text-sm font-medium text-gray-600">잔여 포인트</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {pointHistory.content.map((history) => (
                        <tr key={history.pointHistoryNo} className="hover:bg-gray-50">
                          <td className="px-4 py-3 text-sm text-gray-900">
                            {formatDate(history.createdDate)}
                          </td>
                          <td className="px-4 py-3 text-sm">
                            <span
                              className={`inline-flex px-2 py-1 rounded text-xs font-medium ${
                                history.pointTransactionCode === "001"
                                  ? "bg-green-100 text-green-800"
                                  : "bg-red-100 text-red-800"
                              }`}
                            >
                              {getTransactionTypeName(history.pointTransactionCode)}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-600">
                            {getReasonName(history.pointTransactionReasonCode)}
                            {history.pointTransactionReasonNo && ` (${history.pointTransactionReasonNo})`}
                          </td>
                          <td className="px-4 py-3 text-sm text-right font-medium">
                            {history.pointTransactionCode === "001" ? "+" : "-"}
                            {history.amount.toLocaleString()}원
                          </td>
                          <td className="px-4 py-3 text-sm text-right text-gray-600">
                            {history.remainPoint !== undefined && history.remainPoint !== null
                              ? `${history.remainPoint.toLocaleString()}원`
                              : "-"}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* 페이징 */}
                {pointHistory.totalPages > 1 && (
                  <div className="flex justify-center gap-2 mt-6">
                    <button
                      onClick={() => handlePageChange(currentPage - 1)}
                      disabled={currentPage === 0}
                      className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      이전
                    </button>
                    <div className="flex items-center px-4 py-2 text-sm text-gray-600">
                      {currentPage + 1} / {pointHistory.totalPages}
                    </div>
                    <button
                      onClick={() => handlePageChange(currentPage + 1)}
                      disabled={currentPage >= pointHistory.totalPages - 1}
                      className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      다음
                    </button>
                  </div>
                )}
              </>
            ) : (
              <div className="text-center py-8 text-gray-500">
                포인트 내역이 없습니다
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
