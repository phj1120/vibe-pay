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
    <div className="bg-gray-50 min-h-[calc(100vh-200px)]">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="bg-white rounded-lg shadow-sm p-8 text-center">
          {/* 성공 아이콘 */}
          <div className="mb-6">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto">
              <svg
                className="w-12 h-12 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
          </div>

          {/* 메시지 */}
          <h1 className="text-3xl font-bold mb-4">주문이 완료되었습니다</h1>
          <p className="text-gray-600 mb-8">
            주문해 주셔서 감사합니다.<br />
            주문 내역은 마이페이지에서 확인하실 수 있습니다.
          </p>

          {/* 주문 번호 */}
          <div className="bg-gray-50 rounded-lg p-4 mb-8">
            <div className="text-sm text-gray-600 mb-1">주문번호</div>
            <div className="text-lg font-bold text-blue-600">{orderId}</div>
          </div>

          {/* 안내 메시지 */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-8 text-left">
            <h3 className="font-semibold text-blue-900 mb-2">배송 안내</h3>
            <ul className="text-sm text-blue-800 space-y-1">
              <li>• 주문하신 상품은 2-3일 내에 배송될 예정입니다</li>
              <li>• 배송 관련 문의는 고객센터로 연락주시기 바랍니다</li>
              <li>• 주문 내역은 마이페이지에서 확인하실 수 있습니다</li>
            </ul>
          </div>

          {/* 버튼 그룹 */}
          <div className="flex flex-col sm:flex-row gap-3">
            <button
              onClick={() => router.push("/my-page")}
              className="flex-1 py-3 px-6 border border-gray-300 rounded-md hover:bg-gray-50 font-medium"
            >
              주문 목록 보기
            </button>
            <button
              onClick={() => router.push("/")}
              className="flex-1 py-3 px-6 bg-blue-600 text-white rounded-md hover:bg-blue-700 font-medium"
            >
              쇼핑 계속하기
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
