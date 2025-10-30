"use client";

import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Image from "next/image";
import { getBasketList } from "@/lib/basket-api";
import { memberApi } from "@/lib/member-api";
import { pointApi } from "@/lib/point-api";
import { ApiError } from "@/lib/api-client";
import type { BasketItem } from "@/types/basket";
import type { MemberInfoResponse } from "@/types/member";
import type { PointBalanceResponse } from "@/types/point";

function OrderPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const basketNos = searchParams.get("basketNos")?.split(",") ?? [];

  const [orderItems, setOrderItems] = useState<BasketItem[]>([]);
  const [memberInfo, setMemberInfo] = useState<MemberInfoResponse | null>(null);
  const [pointBalance, setPointBalance] = useState<PointBalanceResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [usePoint, setUsePoint] = useState(0);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      alert("로그인이 필요한 서비스입니다");
      router.push("/login");
      return;
    }

    if (basketNos.length === 0) {
      alert("주문할 상품이 없습니다");
      router.push("/basket");
      return;
    }

    fetchOrderData();
  }, [router]);

  async function fetchOrderData() {
    try {
      setLoading(true);
      const [basketList, member, balance] = await Promise.all([
        getBasketList(),
        memberApi.getMyInfo(),
        pointApi.getBalance(),
      ]);

      const selectedItems = basketList.filter((item) =>
        basketNos.includes(item.basketNo)
      );

      if (selectedItems.length === 0) {
        throw new Error("주문 가능한 상품이 없습니다");
      }

      setOrderItems(selectedItems);
      setMemberInfo(member);
      setPointBalance(balance);
      setError("");
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
        setError(
          err instanceof Error ? err.message : "주문 정보를 불러올 수 없습니다"
        );
      }
    } finally {
      setLoading(false);
    }
  }

  function formatPrice(price: number): string {
    return price.toLocaleString("ko-KR") + "원";
  }

  const totalProductPrice = orderItems.reduce(
    (sum, item) => sum + item.salePrice * item.quantity,
    0
  );

  const deliveryFee = totalProductPrice >= 50000 ? 0 : 3000;
  const totalPayment = totalProductPrice + deliveryFee - usePoint;

  function handleUseAllPoints() {
    if (!pointBalance) return;
    const maxUsePoint = Math.min(pointBalance.totalPoint, totalProductPrice + deliveryFee);
    setUsePoint(maxUsePoint);
  }

  function handlePayment() {
    // TODO: PG 결제 연동
    alert("PG 결제 기능은 추후 구현될 예정입니다");

    // 임시로 주문 완료 페이지로 이동
    router.push("/order/complete?orderId=TEMP_ORDER_001");
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[calc(100vh-200px)]">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm mb-4">
          {error}
        </div>
        <button
          onClick={() => router.push("/basket")}
          className="px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          장바구니로 돌아가기
        </button>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-[calc(100vh-200px)]">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-3xl font-bold mb-8">주문서</h1>

        {/* 주문자 정보 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-bold mb-4">주문자 정보</h2>
          {memberInfo && (
            <div className="space-y-3">
              <div className="flex">
                <span className="w-24 text-gray-600">이름</span>
                <span className="font-medium">{memberInfo.memberName}</span>
              </div>
              <div className="flex">
                <span className="w-24 text-gray-600">이메일</span>
                <span className="font-medium">{memberInfo.email}</span>
              </div>
              <div className="flex">
                <span className="w-24 text-gray-600">연락처</span>
                <span className="font-medium">{memberInfo.phone}</span>
              </div>
            </div>
          )}
        </div>

        {/* 주문 상품 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-bold mb-4">주문 상품</h2>
          <div className="space-y-4">
            {orderItems.map((item) => (
              <div key={item.basketNo} className="flex items-center gap-4 pb-4 border-b last:border-0">
                <div className="relative w-20 h-20 flex-shrink-0">
                  <Image
                    src={item.goodsMainImageUrl}
                    alt={item.goodsName}
                    fill
                    className="object-cover rounded-md"
                  />
                </div>
                <div className="flex-1">
                  <h3 className="font-semibold mb-1">{item.goodsName}</h3>
                  <p className="text-sm text-gray-600">{item.itemName}</p>
                  <p className="text-sm text-gray-600">수량: {item.quantity}개</p>
                </div>
                <div className="text-right">
                  <p className="font-bold">{formatPrice(item.salePrice * item.quantity)}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 포인트 사용 */}
        {pointBalance && (
          <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <h2 className="text-xl font-bold mb-4">포인트 사용</h2>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">보유 포인트</span>
                <span className="font-bold text-blue-600">
                  {formatPrice(pointBalance.totalPoint)}
                </span>
              </div>
              <div className="flex gap-2">
                <input
                  type="number"
                  value={usePoint}
                  onChange={(e) => {
                    const value = Math.max(0, Math.min(pointBalance.totalPoint, Number(e.target.value)));
                    setUsePoint(value);
                  }}
                  placeholder="사용할 포인트"
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={handleUseAllPoints}
                  className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
                >
                  전액사용
                </button>
              </div>
            </div>
          </div>
        )}

        {/* 결제 정보 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-bold mb-4">결제 정보</h2>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-gray-600">상품 금액</span>
              <span>{formatPrice(totalProductPrice)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">배송비</span>
              <span>
                {deliveryFee === 0 ? (
                  <span className="text-green-600">무료</span>
                ) : (
                  formatPrice(deliveryFee)
                )}
              </span>
            </div>
            {usePoint > 0 && (
              <div className="flex justify-between text-red-600">
                <span>포인트 사용</span>
                <span>-{formatPrice(usePoint)}</span>
              </div>
            )}
            <div className="border-t pt-3 flex justify-between items-center">
              <span className="text-lg font-bold">총 결제 금액</span>
              <span className="text-2xl font-bold text-blue-600">
                {formatPrice(totalPayment)}
              </span>
            </div>
          </div>
        </div>

        {/* 결제 버튼 */}
        <button
          onClick={handlePayment}
          className="w-full py-4 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-lg font-semibold"
        >
          {formatPrice(totalPayment)} 결제하기
        </button>
      </div>
    </div>
  );
}

export default function OrderPage() {
  return (
    <Suspense fallback={
      <div className="flex justify-center items-center min-h-[calc(100vh-200px)]">
        <div className="text-lg">로딩 중...</div>
      </div>
    }>
      <OrderPageContent />
    </Suspense>
  );
}
