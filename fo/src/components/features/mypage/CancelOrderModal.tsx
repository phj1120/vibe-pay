"use client";

import { useState, useEffect } from "react";
import { getCancelableOrders, cancelOrder } from "@/lib/order-api";
import type { CancelableOrderResponse, CancelableOrderItem } from "@/types/order";
import AlertModal from "@/components/common/AlertModal";
import ConfirmModal from "@/components/common/ConfirmModal";
import { useAlert } from "@/hooks/useAlert";

interface CancelOrderModalProps {
  orderNo: string;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export default function CancelOrderModal({
  orderNo,
  isOpen,
  onClose,
  onSuccess,
}: CancelOrderModalProps) {
  const [loading, setLoading] = useState(true);
  const [cancelableData, setCancelableData] = useState<CancelableOrderResponse | null>(null);
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set());
  const [canceling, setCanceling] = useState(false);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [resultModalOpen, setResultModalOpen] = useState(false);
  const [resultMessage, setResultMessage] = useState("");
  const [isSuccess, setIsSuccess] = useState(false);
  const alert = useAlert();

  useEffect(() => {
    if (isOpen && orderNo) {
      loadCancelableOrders();
    }
  }, [isOpen, orderNo]);

  const loadCancelableOrders = async () => {
    try {
      setLoading(true);
      const data = await getCancelableOrders(orderNo);
      setCancelableData(data);
      // 기본적으로 모든 항목 선택
      const allKeys = new Set(
        data.cancelableItems.map(
          (item) => `${item.orderSequence}-${item.orderProcessSequence}`
        )
      );
      setSelectedItems(allKeys);
    } catch (error: any) {
      console.error("취소 가능한 주문 조회 실패:", error);
      alert.showAlert(error.message || "취소 가능한 주문 조회에 실패했습니다.");
      onClose();
    } finally {
      setLoading(false);
    }
  };

  const handleToggleItem = (item: CancelableOrderItem) => {
    const key = `${item.orderSequence}-${item.orderProcessSequence}`;
    setSelectedItems((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(key)) {
        newSet.delete(key);
      } else {
        newSet.add(key);
      }
      return newSet;
    });
  };

  const handleCancelClick = () => {
    if (selectedItems.size === 0) {
      alert.showAlert("취소할 상품을 선택해주세요.");
      return;
    }
    setConfirmModalOpen(true);
  };

  const handleCancelConfirm = async () => {
    setConfirmModalOpen(false);
    
    try {
      setCanceling(true);
      const targets = Array.from(selectedItems).map((key) => {
        const [orderSequence, orderProcessSequence] = key.split("-");
        return {
          orderNo,
          orderSequence: Number(orderSequence),
          orderProcessSequence: Number(orderProcessSequence),
        };
      });

      await cancelOrder({ targets });
      
      // 성공 결과 모달 표시
      setIsSuccess(true);
      setResultMessage("주문이 성공적으로 취소되었습니다.");
      setResultModalOpen(true);
    } catch (error: any) {
      console.error("주문 취소 실패:", error);
      
      // 실패 결과 모달 표시
      setIsSuccess(false);
      setResultMessage(error.message || "주문 취소에 실패했습니다.");
      setResultModalOpen(true);
    } finally {
      setCanceling(false);
    }
  };

  const handleResultClose = () => {
    setResultModalOpen(false);
    if (isSuccess) {
      onSuccess();
      onClose();
    }
  };

  const formatPrice = (price: number): string => {
    return price.toLocaleString("ko-KR") + "원";
  };

  // 선택된 상품들의 총 금액 계산
  const calculateSelectedAmount = (): number => {
    if (!cancelableData) return 0;

    return cancelableData.cancelableItems
      .filter((item) => {
        const key = `${item.orderSequence}-${item.orderProcessSequence}`;
        return selectedItems.has(key);
      })
      .reduce((sum, item) => sum + item.subtotal, 0);
  };

  // 선택된 금액 기준으로 환불 상세 계산 (우선순위 순서대로 차감)
  const calculateRefundDetails = () => {
    if (!cancelableData) return { details: [], total: 0 };

    const selectedAmount = calculateSelectedAmount();

    if (selectedAmount === 0) {
      return { details: [], total: 0 };
    }

    let remainingAmount = selectedAmount;
    const details = [];

    // 결제 수단별로 순서대로 차감 (이미 PAY002의 displaySequence 순서로 정렬되어 있음)
    for (const detail of cancelableData.refundInfo.refundDetails) {
      if (remainingAmount <= 0) {
        // 남은 금액이 없으면 0원으로 추가
        details.push({
          ...detail,
          refundAmount: 0,
        });
      } else if (remainingAmount >= detail.refundAmount) {
        // 해당 결제 수단의 전액 환불
        details.push({
          ...detail,
          refundAmount: detail.refundAmount,
        });
        remainingAmount -= detail.refundAmount;
      } else {
        // 남은 금액만큼만 환불
        details.push({
          ...detail,
          refundAmount: remainingAmount,
        });
        remainingAmount = 0;
      }
    }

    // 환불 금액이 0원인 항목은 제외
    const filteredDetails = details.filter((detail) => detail.refundAmount > 0);
    const total = filteredDetails.reduce((sum, detail) => sum + detail.refundAmount, 0);

    return { details: filteredDetails, total };
  };

  const { details: calculatedRefundDetails, total: calculatedRefundTotal } =
    calculateRefundDetails();

  if (!isOpen) return null;

  return (
    <>
      <AlertModal
        isOpen={alert.isOpen}
        message={alert.message}
        onClose={alert.hideAlert}
      />
      <div
        className="fixed inset-0 flex items-center justify-center z-50 p-4"
        style={{
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          backdropFilter: 'blur(4px)',
          WebkitBackdropFilter: 'blur(4px)'
        }}
      >
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* 헤더 */}
        <div className="sticky top-0 bg-white border-b px-6 py-4 flex justify-between items-center">
          <h2 className="text-lg font-semibold">주문 취소</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
            disabled={canceling}
          >
            ×
          </button>
        </div>

        {/* 내용 */}
        <div className="p-6">
          {loading ? (
            <div className="text-center py-12 text-gray-400">로딩 중...</div>
          ) : cancelableData ? (
            <>
              {/* 주문번호 */}
              <div className="mb-6">
                <p className="text-sm text-gray-600">주문번호</p>
                <p className="text-base font-medium">{cancelableData.orderNo}</p>
              </div>

              {/* 취소 가능한 상품 목록 */}
              <div className="mb-6">
                <h3 className="text-sm font-semibold mb-3">취소할 상품 선택</h3>
                <div className="space-y-3">
                  {cancelableData.cancelableItems.map((item) => {
                    const key = `${item.orderSequence}-${item.orderProcessSequence}`;
                    const isSelected = selectedItems.has(key);

                    return (
                      <div
                        key={key}
                        className={`border rounded p-4 cursor-pointer transition ${
                          isSelected
                            ? "border-blue-500 bg-blue-50"
                            : "border-gray-200 hover:border-gray-300"
                        }`}
                        onClick={() => handleToggleItem(item)}
                      >
                        <div className="flex items-start gap-3">
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={() => handleToggleItem(item)}
                            onClick={(e) => e.stopPropagation()}
                            className="mt-1 flex-shrink-0"
                          />
                          <div className="flex-1">
                            <h4 className="text-sm font-medium mb-1">
                              {item.goodsName} - {item.itemName}
                            </h4>
                            <p className="text-xs text-gray-600 mb-2">
                              {formatPrice(item.salePrice)} × {item.quantity}개
                            </p>
                            <p className="text-sm font-medium text-gray-900">
                              {formatPrice(item.subtotal)}
                            </p>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* 환불 정보 */}
              <div className="bg-gray-50 rounded-lg p-4">
                <h3 className="text-sm font-semibold mb-3">환불 정보</h3>

                {calculatedRefundDetails.length > 0 ? (
                  <>
                    {/* 결제 수단별 환불 내역 */}
                    <div className="space-y-2 mb-4">
                      {calculatedRefundDetails.map((detail, index) => (
                        <div key={index} className="flex justify-between text-sm">
                          <span className="text-gray-600">
                            {detail.payWayName}
                            {detail.pgTypeName && ` (${detail.pgTypeName})`}
                          </span>
                          <span className="font-medium">
                            {formatPrice(detail.refundAmount)}
                          </span>
                        </div>
                      ))}
                    </div>

                    {/* 총 환불 금액 */}
                    <div className="border-t pt-3 flex justify-between items-center">
                      <span className="text-sm font-semibold">총 환불 예정 금액</span>
                      <span className="text-lg font-bold text-blue-600">
                        {formatPrice(calculatedRefundTotal)}
                      </span>
                    </div>
                  </>
                ) : (
                  <div className="text-center py-4 text-sm text-gray-500">
                    취소할 상품을 선택해주세요
                  </div>
                )}
              </div>
            </>
          ) : null}
        </div>

        {/* 푸터 */}
        <div className="sticky bottom-0 bg-white border-t px-6 py-4 flex gap-3">
          <button
            onClick={onClose}
            disabled={canceling}
            className="flex-1 px-4 py-2.5 bg-gray-800 text-white hover:bg-gray-900 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            닫기
          </button>
          <button
            onClick={handleCancelClick}
            disabled={canceling || selectedItems.size === 0}
            className="flex-1 px-4 py-2.5 bg-black text-white hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            {canceling ? "취소 처리 중..." : "선택 상품 취소"}
          </button>
        </div>
      </div>
    </div>

    {/* 취소 확인 모달 */}
    <ConfirmModal
      isOpen={confirmModalOpen}
      title="주문 취소 확인"
      message={`선택한 ${selectedItems.size}개 상품을 취소하시겠습니까?`}
      confirmText="취소하기"
      cancelText="닫기"
      variant="danger"
      onConfirm={handleCancelConfirm}
      onCancel={() => setConfirmModalOpen(false)}
    />

    {/* 취소 결과 모달 */}
    <AlertModal
      isOpen={resultModalOpen}
      message={resultMessage}
      onClose={handleResultClose}
    />
    </>
  );
}
