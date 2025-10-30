"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import Cookies from "js-cookie";
import { getOrderSheet } from "@/lib/order-api";
import { getPointBalance } from "@/lib/point-api";
import { ApiError } from "@/lib/api-client";
import type { OrderSheet } from "@/types/order";
import type { PointBalanceResponse } from "@/types/point";

export default function OrderSheetPage() {
  const router = useRouter();
  const [orderSheet, setOrderSheet] = useState<OrderSheet | null>(null);
  const [pointBalance, setPointBalance] = useState<PointBalanceResponse | null>(null);
  const [usePoint, setUsePoint] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");
  const [showErrorModal, setShowErrorModal] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      router.push("/login");
      return;
    }

    fetchOrderSheet();
  }, [router]);

  async function fetchOrderSheet() {
    try {
      setLoading(true);

      // 쿠키에서 장바구니 번호 목록 읽기
      const basketNoList = Cookies.get("basket_no_list");
      if (!basketNoList) {
        setError("주문할 상품을 선택해주세요");
        setShowErrorModal(true);
        return;
      }

      const basketNos = basketNoList.split(",");

      // 주문서 정보 조회
      const sheetData = await getOrderSheet(basketNos);
      setOrderSheet(sheetData);

      // 쿠키 삭제
      Cookies.remove("basket_no_list");

      // 포인트 조회 (실패해도 화면 표시)
      try {
        const balance = await getPointBalance();
        setPointBalance(balance);
      } catch (err) {
        console.error("포인트 조회 실패:", err);
        // 포인트 조회 실패는 무시하고 계속 진행
      }

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
        setError("주문서를 불러오는 중 오류가 발생했습니다");
      }
      setShowErrorModal(true);
    } finally {
      setLoading(false);
    }
  }

  function handleErrorModalClose() {
    setShowErrorModal(false);
    router.push("/basket");
  }

  function formatPrice(price: number): string {
    return price.toLocaleString("ko-KR") + "원";
  }

  function handleUseAllPoint() {
    if (!pointBalance || !orderSheet) return;

    const availablePoint = pointBalance.totalPoint;
    const maxUsablePoint = orderSheet.totalProductAmount - 101; // 최소 결제금액 100원 초과 유지

    if (maxUsablePoint <= 0) {
      alert("포인트를 사용할 수 없습니다");
      return;
    }

    const pointToUse = Math.min(availablePoint, maxUsablePoint);
    setUsePoint(pointToUse);
  }

  function handlePointChange(value: string) {
    if (!orderSheet || !pointBalance) return;

    const numValue = parseInt(value) || 0;

    // 보유 포인트 초과 확인
    if (numValue > pointBalance.totalPoint) {
      alert("보유 포인트를 초과할 수 없습니다");
      return;
    }

    // 최소 결제 금액 100원 초과 유지 확인
    const remainingAmount = orderSheet.totalProductAmount - numValue;
    if (remainingAmount <= 100) {
      alert("카드 결제 금액이 최소 100원 초과되어야 합니다");
      return;
    }

    setUsePoint(numValue);
  }

  function handlePayment() {
    if (!orderSheet) return;

    const finalAmount = orderSheet.totalProductAmount - usePoint;

    if (finalAmount <= 100) {
      alert("결제 금액은 100원을 초과해야 합니다");
      return;
    }

    // TODO: 실제 결제 처리 로직 구현
    console.log("결제 처리:", {
      totalAmount: orderSheet.totalProductAmount,
      usePoint,
      finalAmount,
      items: orderSheet.items,
    });

    alert("결제 기능은 추후 구현 예정입니다");
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-sm text-gray-400">로딩 중...</div>
      </div>
    );
  }

  if (showErrorModal) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white p-8 rounded-lg max-w-md">
          <h2 className="text-lg font-medium mb-4">오류</h2>
          <p className="text-sm text-gray-700 mb-6">{error}</p>
          <button
            onClick={handleErrorModalClose}
            className="w-full py-3 bg-black text-white text-sm hover:bg-gray-800"
          >
            확인
          </button>
        </div>
      </div>
    );
  }

  if (!orderSheet) {
    return null;
  }

  const finalPaymentAmount = orderSheet.totalProductAmount - usePoint;

  return (
    <div className="bg-white min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-2xl font-medium mb-12">주문서</h1>

        {/* 주문자 정보 */}
        <div className="mb-12">
          <h2 className="text-lg font-medium mb-6">주문자 정보</h2>
          <div className="border border-gray-200 p-6 space-y-3">
            <div className="flex">
              <span className="w-24 text-sm text-gray-600">이름</span>
              <span className="text-sm">{orderSheet.ordererName}</span>
            </div>
            <div className="flex">
              <span className="w-24 text-sm text-gray-600">이메일</span>
              <span className="text-sm">{orderSheet.ordererEmail}</span>
            </div>
            <div className="flex">
              <span className="w-24 text-sm text-gray-600">연락처</span>
              <span className="text-sm">{orderSheet.ordererPhone}</span>
            </div>
          </div>
        </div>

        {/* 주문 상품 목록 */}
        <div className="mb-12">
          <h2 className="text-lg font-medium mb-6">주문 상품</h2>
          <div className="space-y-4">
            {orderSheet.items.map((item) => (
              <div
                key={item.basketNo}
                className="flex items-center gap-6 p-6 border border-gray-200"
              >
                <div className="relative w-24 h-24 flex-shrink-0 bg-gray-100">
                  <Image
                    src={item.goodsMainImageUrl}
                    alt={item.goodsName}
                    fill
                    className="object-cover"
                  />
                </div>

                <div className="flex-1">
                  <h3 className="font-medium mb-1">{item.goodsName}</h3>
                  <p className="text-sm text-gray-600">{item.itemName}</p>
                  <p className="text-sm text-gray-900 mt-1">
                    {formatPrice(item.salePrice)} × {item.quantity}개
                  </p>
                </div>

                <div className="text-right">
                  <p className="font-medium">
                    {formatPrice(item.salePrice * item.quantity)}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 포인트 사용 */}
        <div className="mb-12">
          <h2 className="text-lg font-medium mb-6">포인트 사용</h2>
          <div className="border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <span className="text-sm text-gray-600">보유 포인트</span>
              <span className="text-sm font-medium">
                {pointBalance ? formatPrice(pointBalance.totalPoint) : "조회 실패"}
              </span>
            </div>
            <div className="flex gap-2">
              <input
                type="number"
                value={usePoint}
                onChange={(e) => handlePointChange(e.target.value)}
                placeholder="사용할 포인트"
                className="flex-1 px-4 py-3 border border-gray-300 text-sm"
                disabled={!pointBalance}
              />
              <button
                onClick={handleUseAllPoint}
                disabled={!pointBalance}
                className="px-6 py-3 border border-gray-300 text-sm hover:bg-gray-50 disabled:bg-gray-100 disabled:text-gray-400"
              >
                전액 사용
              </button>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              * 카드 결제 금액이 최소 100원 초과되어야 합니다
            </p>
          </div>
        </div>

        {/* 결제 금액 */}
        <div className="mb-12">
          <h2 className="text-lg font-medium mb-6">결제 금액</h2>
          <div className="border border-gray-200 p-6 space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">총 상품 금액</span>
              <span className="text-sm">{formatPrice(orderSheet.totalProductAmount)}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">포인트 사용</span>
              <span className="text-sm text-red-600">-{formatPrice(usePoint)}</span>
            </div>
            <div className="border-t pt-3 mt-3">
              <div className="flex justify-between items-center">
                <span className="text-base font-medium">최종 결제 금액</span>
                <span className="text-2xl font-medium">
                  {formatPrice(finalPaymentAmount)}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* 결제 수단 */}
        <div className="mb-12">
          <h2 className="text-lg font-medium mb-6">결제 수단</h2>
          <div className="border border-gray-200 p-6">
            <div className="flex items-center gap-3">
              <input
                type="radio"
                id="card"
                name="paymentMethod"
                checked
                readOnly
                className="w-4 h-4"
              />
              <label htmlFor="card" className="text-sm">카드 결제</label>
            </div>
          </div>
        </div>

        {/* 결제하기 버튼 */}
        <button
          onClick={handlePayment}
          className="w-full py-4 bg-black text-white text-sm hover:bg-gray-800"
        >
          결제하기
        </button>
      </div>
    </div>
  );
}
