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
  const [orderFailureModal, setOrderFailureModal] = useState(false);
  const [orderFailureMessage, setOrderFailureMessage] = useState<string>("");
  const [isPaymentProcessing, setIsPaymentProcessing] = useState(false);

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

  function handleOrderFailureModalClose() {
    setOrderFailureModal(false);
    setOrderFailureMessage("");
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

  async function handlePayment() {
    if (!orderSheet) return;

    const finalAmount = orderSheet.totalProductAmount - usePoint;

    if (finalAmount <= 100) {
      alert("결제 금액은 100원을 초과해야 합니다");
      return;
    }

    try {
      setIsPaymentProcessing(true);

      // 1. 주문번호 생성
      const { generateOrderNumber } = await import("@/lib/order-api");
      const orderNumber = await generateOrderNumber();

      // 2. 결제 초기화 (PG사 선택 및 폼 데이터 생성)
      const { initiatePayment } = await import("@/lib/order-api");
      const paymentInitResponse = await initiatePayment({
        orderNumber,
        amount: finalAmount,
        productName: orderSheet.items.length > 1
          ? `${orderSheet.items[0].goodsName} 외 ${orderSheet.items.length - 1}건`
          : orderSheet.items[0].goodsName,
        buyerName: orderSheet.ordererName,
        buyerEmail: orderSheet.ordererEmail,
        buyerTel: orderSheet.ordererPhone,
      });

      // 3. 쿠키에 주문 정보 저장 (5분 유효)
      const { setOrderCookie } = await import("@/lib/order-cookie");
      setOrderCookie({
        orderNumber,
        orderInfo: {
          ordererInfo: {
            name: orderSheet.ordererName,
            phone: orderSheet.ordererPhone,
            email: orderSheet.ordererEmail,
          },
          deliveryInfo: {
            recipientName: orderSheet.ordererName, // 임시로 주문자명 사용
            phone: orderSheet.ordererPhone,
            zipCode: '',
            address: '',
          },
          paymentInfo: {
            paymentMethod: 'CARD' as const,
            pgType: paymentInitResponse.pgType,
          },
          products: orderSheet.items.map(item => ({
            goodsNo: item.goodsNo,
            goodsName: item.goodsName,
            price: item.salePrice,
            quantity: item.quantity,
            totalPrice: item.salePrice * item.quantity,
          })),
          totalAmount: orderSheet.totalProductAmount,
          discountAmount: usePoint,
          deliveryFee: 0,
          finalAmount: finalAmount,
        },
        paymentInitiate: paymentInitResponse,
        timestamp: Date.now(),
      });

      // 4. 결제 결과 메시지 수신 - 팝업 열기 전에 등록
      const handlePaymentResult = async (event: MessageEvent) => {
        console.log("Message received:", event.data, "from:", event.origin);
        
        // origin 체크
        if (event.origin !== window.location.origin) {
          console.log("Origin mismatch:", event.origin, "!==", window.location.origin);
          return;
        }

        const { success, authData, error, errorDetails } = event.data;

        // 타입 체크 - 결제 결과 메시지인지 확인
        if (typeof success === 'undefined') {
          return;
        }

        // 이벤트 리스너 제거
        window.removeEventListener("message", handlePaymentResult);
        clearInterval(checkPopupClosed);
        setIsPaymentProcessing(false);

        if (success && authData) {
          // 결제 성공 - 주문 생성 API 호출
          try {
            const { createOrder } = await import("@/lib/order-api");
            const { pgTypeToCode } = await import("@/lib/pg-utils");

            // PayRequest 생성
            const payList = [];

            // PG 타입을 코드값으로 변환
            const pgTypeCode = pgTypeToCode(paymentInitResponse.pgType);

            // PG 타입별로 PaymentConfirmRequest 구성
            let paymentConfirmRequest;

            if (paymentInitResponse.pgType === 'INICIS') {
              const inicisData = authData as any;
              paymentConfirmRequest = {
                pgTypeCode: pgTypeCode,
                authToken: inicisData.authToken || '',
                orderNo: orderNumber,
                authUrl: inicisData.authUrl || '',
                netCancelUrl: inicisData.netCancelUrl || '',
                // Inicis 전용 필드
                price: finalAmount,
              };
            } else if (paymentInitResponse.pgType === 'NICE') {
              const niceData = authData as any;
              paymentConfirmRequest = {
                pgTypeCode: pgTypeCode,
                authToken: niceData.AuthToken || '',
                orderNo: orderNumber,
                authUrl: niceData.NextAppURL || '',  // NextAppURL 필드 사용
                netCancelUrl: niceData.NetCancelURL || '',  // NetCancelURL 필드 사용
                // Nice 전용 필드
                transactionId: niceData.Signature || '',  // Signature가 transactionId
                amount: niceData.Amt || '',
                tradeNo: niceData.TxTid || '',  // TxTid가 거래번호
                mid: niceData.MID || '',
              };
            }

            // 카드 결제
            payList.push({
              payWayCode: "001", // 카드
              amount: finalAmount,
              payTypeCode: "001", // 결제
              paymentConfirmRequest,
            });

            // 포인트 결제
            if (usePoint > 0) {
              payList.push({
                payWayCode: "002", // 포인트
                amount: usePoint,
                payTypeCode: "001", // 결제
                paymentConfirmRequest: null,
              });
            }

            await createOrder({
              orderNo: orderNumber,
              memberName: orderSheet.ordererName,
              phone: orderSheet.ordererPhone,
              email: orderSheet.ordererEmail,
              goodsList: orderSheet.items,
              payList,
            });

            // 주문 완료 페이지로 이동
            router.push(`/order/complete?orderNo=${orderNumber}`);
          } catch (err) {
            console.error("주문 생성 실패:", err);
            
            let errorMessage = "주문 생성에 실패했습니다.";
            
            if (err instanceof ApiError) {
              errorMessage = err.message;
              
              // API 에러인 경우 상세 정보 추가
              errorMessage += `\n\n[상세 정보]`;
              if (err.code) {
                errorMessage += `\n오류 코드: ${err.code}`;
              }
              errorMessage += `\n주문번호: ${orderNumber}`;
              errorMessage += `\n발생 시각: ${new Date().toLocaleString('ko-KR')}`;
              errorMessage += `\n\n결제는 완료되었으나 주문 처리 중 문제가 발생했습니다.`;
              errorMessage += `\n고객센터(주문번호 포함)로 문의해주세요.`;
            } else {
              errorMessage += `\n\n고객센터로 문의해주세요.`;
            }
            
            setOrderFailureMessage(errorMessage);
            setOrderFailureModal(true);
          }
        } else {
          // 결제 실패 - 상세 정보 포함
          let failureMessage = error || "결제에 실패했습니다";
          
          if (errorDetails) {
            failureMessage = `${failureMessage}\n\n[상세 정보]`;
            if (errorDetails.pgType && errorDetails.pgType !== 'UNKNOWN') {
              const pgName = errorDetails.pgType === 'INICIS' ? 'KG이니시스' : '나이스페이';
              failureMessage += `\nPG사: ${pgName}`;
            }
            if (errorDetails.errorCode && errorDetails.errorCode !== 'UNKNOWN') {
              failureMessage += `\n오류 코드: ${errorDetails.errorCode}`;
            }
            if (errorDetails.timestamp) {
              const date = new Date(errorDetails.timestamp);
              failureMessage += `\n발생 시각: ${date.toLocaleString('ko-KR')}`;
            }
          }
          
          setOrderFailureMessage(failureMessage);
          setOrderFailureModal(true);
        }
      };

      // 이벤트 리스너 등록
      window.addEventListener("message", handlePaymentResult);

      // 5. 결제 팝업 열기
      const { openPgPopup } = await import("@/lib/pg-utils");
      const popup = openPgPopup(paymentInitResponse.pgType, "/order/popup");

      if (!popup) {
        window.removeEventListener("message", handlePaymentResult);
        setOrderFailureMessage("팝업 차단을 해제해주세요");
        setOrderFailureModal(true);
        setIsPaymentProcessing(false);
        return;
      }

      // 6. 팝업 닫힘 감지
      const checkPopupClosed = setInterval(() => {
        if (popup.closed) {
          clearInterval(checkPopupClosed);
          window.removeEventListener("message", handlePaymentResult);
          setIsPaymentProcessing(false);
        }
      }, 500);

    } catch (err) {
      console.error("결제 처리 중 오류:", err);
      const errorMessage = err instanceof ApiError 
        ? err.message 
        : "결제 처리 중 오류가 발생했습니다";
      setOrderFailureMessage(errorMessage);
      setOrderFailureModal(true);
      setIsPaymentProcessing(false);
    }
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

  // 주문 실패 모달
  if (orderFailureModal) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white p-8 rounded-lg max-w-md">
          <h2 className="text-lg font-medium mb-4">주문 실패</h2>
          <p className="text-sm text-gray-700 mb-6 whitespace-pre-line">{orderFailureMessage}</p>
          <button
            onClick={handleOrderFailureModalClose}
            className="w-full py-3 bg-black text-white text-sm hover:bg-gray-800"
          >
            장바구니로 이동
          </button>
        </div>
      </div>
    );
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
          disabled={isPaymentProcessing}
          className={`w-full py-4 text-white text-sm ${
            isPaymentProcessing
              ? "bg-gray-400 cursor-not-allowed"
              : "bg-black hover:bg-gray-800"
          }`}
        >
          결제하기
        </button>
      </div>
    </div>
  );
}
