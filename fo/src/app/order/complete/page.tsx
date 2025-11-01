"use client";

import { useEffect, useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { getOrderComplete, type OrderCompleteResponse } from "@/lib/order-api";

function OrderCompleteContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const orderNo = searchParams.get("orderNo");
  const [orderData, setOrderData] = useState<OrderCompleteResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchOrderData = async () => {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        alert("로그인이 필요한 서비스입니다");
        router.push("/login");
        return;
      }

      if (!orderNo) {
        alert("잘못된 접근입니다");
        router.push("/");
        return;
      }

      try {
        const data = await getOrderComplete(orderNo);
        setOrderData(data);
      } catch (err) {
        console.error("주문 완료 정보 조회 실패:", err);
        setError("주문 정보를 불러올 수 없습니다");
        alert("잘못된 접근입니다");
        router.push("/");
      } finally {
        setLoading(false);
      }
    };

    fetchOrderData();
  }, [router, orderNo]);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[calc(100vh-200px)]">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  if (error || !orderData) {
    return null;
  }

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

  const formatPrice = (price: number) => {
    return price.toLocaleString("ko-KR");
  };

  return (
    <div className="bg-white min-h-screen">
      <div className="max-w-lg mx-auto px-4 py-20">
        <div className="text-center mb-12">
          {/* 메시지 */}
          <h1 className="text-2xl font-medium mb-4">주문이 완료되었습니다</h1>
          <p className="text-sm text-gray-600">
            주문해 주셔서 감사합니다
          </p>
        </div>

        {/* 주문 정보 */}
        <div className="space-y-6 mb-12">
          {/* 주문 번호 */}
          <div className="bg-gray-50 p-6">
            <div className="text-xs text-gray-600 mb-2">주문번호</div>
            <div className="text-sm font-medium">{orderData.orderNo}</div>
          </div>

          {/* 주문 일시 */}
          <div className="bg-gray-50 p-6">
            <div className="text-xs text-gray-600 mb-2">주문 일시</div>
            <div className="text-sm font-medium">{formatDate(orderData.orderAcceptDtm)}</div>
          </div>

          {/* 주문 상품 목록 */}
          <div className="border-t pt-6">
            <h2 className="text-base font-medium mb-4">주문 상품</h2>
            <div className="space-y-4">
              {orderData.goodsList.map((goods, index) => (
                <div key={index} className="flex justify-between text-sm">
                  <div>
                    <div className="font-medium">{goods.goodsName}</div>
                    <div className="text-gray-600 text-xs mt-1">
                      {goods.itemName} / 수량: {goods.quantity}개
                    </div>
                  </div>
                  <div className="font-medium">{formatPrice(goods.subtotal)}원</div>
                </div>
              ))}
            </div>
          </div>

          {/* 결제 정보 */}
          <div className="border-t pt-6">
            <h2 className="text-base font-medium mb-4">결제 정보</h2>
            <div className="space-y-3">
              {orderData.paymentList.map((payment, index) => (
                <div key={index} className="flex justify-between text-sm">
                  <div className="text-gray-600">
                    {payment.payWayName}
                    {payment.pgTypeName && ` (${payment.pgTypeName})`}
                  </div>
                  <div className="font-medium">{formatPrice(payment.amount)}원</div>
                </div>
              ))}
              <div className="border-t pt-3 flex justify-between text-base font-semibold">
                <div>총 결제금액</div>
                <div>{formatPrice(orderData.totalAmount)}원</div>
              </div>
            </div>
          </div>
        </div>

        {/* 버튼 그룹 */}
        <div className="space-y-3">
          <button
            onClick={() => router.push("/my-page?tab=order")}
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
