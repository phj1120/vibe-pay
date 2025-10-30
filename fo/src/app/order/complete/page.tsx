"use client";

import { useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";

function OrderCompleteContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const orderId = searchParams.get("orderId");

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      alert("로그인이 필요한 서비스입니다");
      router.push("/login");
      return;
    }

    if (!orderId) {
      alert("잘못된 접근입니다");
      router.push("/");
      return;
    }
  }, [router, orderId]);

  return (
    <div className="bg-white min-h-screen">
      <div className="max-w-lg mx-auto px-4 py-20">
        <div className="text-center">
          {/* 메시지 */}
          <h1 className="text-2xl font-medium mb-4">주문이 완료되었습니다</h1>
          <p className="text-sm text-gray-600 mb-12">
            주문해 주셔서 감사합니다
          </p>

          {/* 주문 번호 */}
          <div className="bg-gray-50 p-6 mb-12">
            <div className="text-xs text-gray-600 mb-2">주문번호</div>
            <div className="text-sm font-medium">{orderId}</div>
          </div>

          {/* 버튼 그룹 */}
          <div className="space-y-3">
            <button
              onClick={() => router.push("/my-page")}
              className="w-full py-3 bg-black text-white text-sm hover:bg-gray-800"
            >
              주문 내역
            </button>
            <button
              onClick={() => router.push("/")}
              className="w-full py-3 border border-gray-300 text-sm hover:border-black"
            >
              계속 쇼핑하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function OrderCompletePage() {
  return (
    <Suspense fallback={
      <div className="flex justify-center items-center min-h-[calc(100vh-200px)]">
        <div className="text-lg">로딩 중...</div>
      </div>
    }>
      <OrderCompleteContent />
    </Suspense>
  );
}
