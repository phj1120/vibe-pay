"use client";

import { useState, useEffect } from "react";
import { getOrderList } from "@/lib/order-api";
import type { OrderListResponse } from "@/types/order";
import CancelOrderModal from "./CancelOrderModal";
import AlertModal from "@/components/common/AlertModal";
import { useAlert } from "@/hooks/useAlert";

export default function OrderListSection() {
  const [orders, setOrders] = useState<OrderListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [selectedOrderNo, setSelectedOrderNo] = useState<string | null>(null);
  const alert = useAlert();

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const data = await getOrderList();
      setOrders(data);
    } catch (error) {
      console.error('주문 목록 조회 실패:', error);
      alert.showAlert('주문 목록 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCancelModal = (orderNo: string) => {
    setSelectedOrderNo(orderNo);
    setCancelModalOpen(true);
  };

  const handleCloseCancelModal = () => {
    setCancelModalOpen(false);
    setSelectedOrderNo(null);
  };

  const handleCancelSuccess = () => {
    loadOrders(); // 주문 목록 새로고침
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

  const formatPrice = (price: number): string => {
    return price.toLocaleString("ko-KR") + "원";
  };

  const getStatusColor = (statusCode: string) => {
    switch (statusCode) {
      case "001":
        return "bg-blue-100 text-blue-800";
      case "002":
        return "bg-green-100 text-green-800";
      case "003":
        return "bg-red-100 text-red-800";
      case "107":
        return "bg-purple-100 text-purple-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  if (loading) {
    return (
      <div className="text-center py-12 text-gray-400 text-sm">
        로딩 중...
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="text-center py-12 text-gray-400 text-sm">
        주문 내역이 없습니다
      </div>
    );
  }

  return (
    <>
      <AlertModal
        isOpen={alert.isOpen}
        message={alert.message}
        onClose={alert.hideAlert}
      />
      <div className="space-y-8">
        {orders.map((order) => (
          <div key={order.orderNo} className="border-b pb-8">
            {/* 주문 헤더 */}
            <div className="flex justify-between items-start mb-6">
              <div>
                <div className="text-sm text-gray-600 mb-1">
                  {formatDate(order.orderAcceptDtm)}
                </div>
                <div className="text-xs text-gray-500">{order.orderNo}</div>
              </div>
              <div className="text-right">
                <div className="text-base font-medium">
                  {formatPrice(order.totalAmount)}
                </div>
              </div>
            </div>

            {/* 주문 상품 목록 */}
            <div className="space-y-4">
              {order.goodsList.map((goods, index) => {
                const key = `${order.orderNo}-${goods.orderSequence}-${goods.orderProcessSequence}`;

                return (
                  <div
                    key={key}
                    className="flex items-start gap-4 p-4 border rounded border-gray-200"
                  >
                    <div className="flex-1">
                      <h4 className="text-sm font-medium mb-1">
                        {goods.goodsName} - {goods.itemName}
                      </h4>
                      <p className="text-xs text-gray-600 mb-2">
                        {formatPrice(goods.salePrice)} × {goods.quantity}개
                      </p>
                      <div className="flex gap-2">
                        <span className="text-xs px-2 py-1 rounded bg-gray-100 text-gray-800">
                          {goods.orderTypeName}
                        </span>
                        {goods.cancelable && (
                          <button
                            onClick={() => handleOpenCancelModal(order.orderNo)}
                            className="text-xs px-3 py-1 bg-black text-white hover:bg-gray-800 rounded transition"
                          >
                            취소하기
                          </button>
                        )}
                      </div>
                    </div>

                    <div className="text-right flex-shrink-0">
                      <p className="text-sm font-medium">
                        {formatPrice(goods.salePrice * goods.quantity)}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* 취소 모달 */}
      {selectedOrderNo && (
        <CancelOrderModal
          orderNo={selectedOrderNo}
          isOpen={cancelModalOpen}
          onClose={handleCloseCancelModal}
          onSuccess={handleCancelSuccess}
        />
      )}
    </>
  );
}
