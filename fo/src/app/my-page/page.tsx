"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { memberApi } from "@/lib/member-api";
import { ApiError } from "@/lib/api-client";
import type { MemberInfoResponse } from "@/types/member";

export default function MyPage() {
  const router = useRouter();
  const [memberInfo, setMemberInfo] = useState<MemberInfoResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    const fetchMemberInfo = async () => {
      try {
        const token = localStorage.getItem("accessToken");
        if (!token) {
          router.push("/login");
          return;
        }

        const data = await memberApi.getMyInfo();
        setMemberInfo(data);
      } catch (err) {
        if (err instanceof ApiError) {
          if (err.code === "2001" || err.code === "2002" || err.code === "2003") {
            // 인증 오류인 경우 로그인 페이지로 이동
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            router.push("/login");
            return;
          }
          setError(err.message);
        } else {
          setError("회원 정보를 불러오는 중 오류가 발생했습니다");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchMemberInfo();
  }, [router]);

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
      </div>
    </div>
  );
}
