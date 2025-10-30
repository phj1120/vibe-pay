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
} from "@/types/point";
import MemberInfoSection from "@/components/features/mypage/MemberInfoSection";
import PointSection from "@/components/features/mypage/PointSection";
import OrderListSection from "@/components/features/mypage/OrderListSection";

type TabType = "member" | "point" | "order";

export default function MyPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<TabType>("member");
  const [memberInfo, setMemberInfo] = useState<MemberInfoResponse | null>(null);
  const [pointBalance, setPointBalance] = useState<PointBalanceResponse | null>(null);
  const [pointHistory, setPointHistory] = useState<PointHistoryListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    fetchData();
  }, [router]);

  async function fetchData() {
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
  }

  function handleLogout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    alert("로그아웃 되었습니다");
    router.push("/login");
  };

  const tabs = [
    { id: "member" as TabType, label: "회원정보" },
    { id: "point" as TabType, label: "포인트" },
    { id: "order" as TabType, label: "주문내역" },
  ];

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="text-sm text-gray-400">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="w-full max-w-sm text-center">
          <div className="text-sm text-gray-900 mb-6">
            {error}
          </div>
          <button
            onClick={() => router.push("/login")}
            className="px-8 py-3 bg-black text-white text-sm hover:bg-gray-800"
          >
            로그인
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-4xl mx-auto px-4 py-12">
        {/* 헤더 */}
        <div className="flex justify-between items-center mb-12">
          <h1 className="text-2xl font-medium">마이페이지</h1>
          <button
            onClick={handleLogout}
            className="text-sm text-gray-600 hover:text-black"
          >
            로그아웃
          </button>
        </div>

        {/* 탭 네비게이션 */}
        <div className="border-b mb-8">
          <div className="flex gap-8">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`pb-4 text-sm transition-colors relative ${
                  activeTab === tab.id
                    ? "text-black"
                    : "text-gray-400 hover:text-gray-600"
                }`}
              >
                {tab.label}
                {activeTab === tab.id && (
                  <div className="absolute bottom-0 left-0 right-0 h-px bg-black" />
                )}
              </button>
            ))}
          </div>
        </div>

        {/* 탭 컨텐츠 */}
        <div>
          {activeTab === "member" && <MemberInfoSection memberInfo={memberInfo} />}
          {activeTab === "point" && (
            <PointSection
              pointBalance={pointBalance}
              pointHistory={pointHistory}
              onRefresh={fetchData}
            />
          )}
          {activeTab === "order" && <OrderListSection />}
        </div>
      </div>
    </div>
  );
}
