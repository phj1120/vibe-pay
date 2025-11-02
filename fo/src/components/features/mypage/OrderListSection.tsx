"use client";

import { useState, useEffect } from "react";
import { getOrderList, cancelOrder } from "@/lib/order-api";
import type { OrderListResponse } from "@/types/order";

export default function OrderListSection() {
  const [orders, setOrders] = useState<OrderListResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set());

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
      alert('주문 목록 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectItem = (orderNo: string, orderSequence: number, orderProcessSequence: number) => {
    const key = `${orderNo}-${orderSequence}-${orderProcessSequence}`;
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

  const handleCancelOrder = async () => {
    if (selectedItems.size === 0) {
      alert('취소할 상품을 선택해주세요.');
      return;
    }

    if (!confirm(`선택한 ${selectedItems.size}개 상품을 취소하시겠습니까?`)) {
      return;
    }

    try {
      const targets = Array.from(selectedItems).map((key) => {
        const [orderNo, orderSequence, orderProcessSequence] = key.split('-');
        return {
          orderNo,
          orderSequence: Number(orderSequence),
          orderProcessSequence: Number(orderProcessSequence),
        };
      });

      await cancelOrder({ targets });
      alert('주문이 취소되었습니다.');
      setSelectedItems(new Set());
      await loadOrders();
    } catch (error: any) {
      console.error('주문 취소 실패:', error);
      alert(error.message || '주문 취소에 실패했습니다.');
    }
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
      case "003":
        return "bg-red-100 text-red-800";
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
    <div>
      {/* 취소 버튼 */}
      {selectedItems.size > 0 && (
        <div className="mb-6 flex justify-end">
          <button
            onClick={handleCancelOrder}
            className="px-6 py-2.5 bg-red-500 text-white hover:bg-red-600 text-sm transition"
          >
            선택 상품 취소 ({selectedItems.size})
          </button>
        </div>
      )}

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
              {order.goodsList.map((goods) => {
                const key = `${order.orderNo}-${goods.orderSequence}-${goods.orderProcessSequence}`;
                const isSelected = selectedItems.has(key);

                return (
                  <div
                    key={key}
                    className={`flex items-start gap-4 p-4 border rounded transition ${
                      isSelected ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    {goods.cancelable && (
                      <input
                        type="checkbox"
                        checked={isSelected}
                        onChange={() =>
                          handleSelectItem(
                            order.orderNo,
                            goods.orderSequence,
                            goods.orderProcessSequence
                          )
                        }
                        className="mt-1 flex-shrink-0"
                      />
                    )}

                    <div className="flex-1">
                      <h4 className="text-sm font-medium mb-1">
                        {goods.goodsName} - {goods.itemName}
                      </h4>
                      <p className="text-xs text-gray-600 mb-2">
                        {formatPrice(goods.salePrice)} × {goods.quantity}개
                      </p>
                      <div className="flex gap-2">
                        <span
                          className={`text-xs px-2 py-1 rounded ${getStatusColor(
                            goods.orderStatusCode
                          )}`}
                        >
                          {goods.orderStatusName}
                        </span>
                        <span className="text-xs px-2 py-1 rounded bg-gray-100 text-gray-800">
                          {goods.orderTypeName}
                        </span>
                      </div>
                    </div>

                    <div className="text-right flex-shrink-0">
                      <p className="text-sm font-medium">
                        {formatPrice(goods.salePrice * goods.quantity)}
                      </p>
                      {goods.cancelable && goods.cancelableAmount > 0 && (
                        <p className="text-xs text-gray-500 mt-1">
                          취소가능: {formatPrice(goods.cancelableAmount)}
                        </p>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
