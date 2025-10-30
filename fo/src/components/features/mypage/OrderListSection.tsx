"use client";

import { useState } from "react";
import Image from "next/image";

// 더미 데이터 타입
interface OrderGoods {
  goodsNo: string;
  goodsName: string;
  itemNo: string;
  itemName: string;
  quantity: number;
  salePrice: number;
  goodsMainImageUrl: string;
}

interface Order {
  orderNo: string;
  orderDate: string;
  orderStatusCode: string;
  orderStatusName: string;
  totalAmount: number;
  goods: OrderGoods[];
}

// 더미 데이터
const DUMMY_ORDERS: Order[] = [
  {
    orderNo: "20251029-833701",
    orderDate: "2025-10-29T15:30:00",
    orderStatusCode: "001",
    orderStatusName: "주문완료",
    totalAmount: 455000,
    goods: [
      {
        goodsNo: "G001",
        goodsName: "DKNY 세미 와이드 팬츠",
        itemNo: "I001",
        itemName: "CHARCOAL GREY 32 1개",
        quantity: 1,
        salePrice: 455000,
        goodsMainImageUrl: "https://via.placeholder.com/80",
      },
    ],
  },
  {
    orderNo: "20251028-723456",
    orderDate: "2025-10-28T14:20:00",
    orderStatusCode: "002",
    orderStatusName: "배송중",
    totalAmount: 598000,
    goods: [
      {
        goodsNo: "G002",
        goodsName: "크롬 니트 가디건",
        itemNo: "I002",
        itemName: "BROWN XS 1개",
        quantity: 1,
        salePrice: 350000,
        goodsMainImageUrl: "https://via.placeholder.com/80",
      },
      {
        goodsNo: "G003",
        goodsName: "베이직 티셔츠",
        itemNo: "I003",
        itemName: "WHITE M 2개",
        quantity: 2,
        salePrice: 124000,
        goodsMainImageUrl: "https://via.placeholder.com/80",
      },
    ],
  },
  {
    orderNo: "20251027-612345",
    orderDate: "2025-10-27T10:15:00",
    orderStatusCode: "003",
    orderStatusName: "배송완료",
    totalAmount: 395000,
    goods: [
      {
        goodsNo: "G004",
        goodsName: "A라인 스커트",
        itemNo: "I004",
        itemName: "GREY FREE 1개",
        quantity: 1,
        salePrice: 395000,
        goodsMainImageUrl: "https://via.placeholder.com/80",
      },
    ],
  },
];

export default function OrderListSection() {
  const [orders] = useState<Order[]>(DUMMY_ORDERS);
  const [currentPage, setCurrentPage] = useState(0);
  const totalPages = 1; // 더미 데이터는 1페이지만

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
        return "bg-yellow-100 text-yellow-800";
      case "003":
        return "bg-green-100 text-green-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  if (orders.length === 0) {
    return (
      <div className="text-center py-12 text-gray-400 text-sm">
        주문 내역이 없습니다
      </div>
    );
  }

  return (
    <div>
      <div className="space-y-8">
        {orders.map((order) => (
          <div key={order.orderNo} className="border-b pb-8">
            {/* 주문 헤더 */}
            <div className="flex justify-between items-start mb-6">
              <div>
                <div className="text-sm text-gray-600 mb-1">
                  {formatDate(order.orderDate)}
                </div>
                <div className="text-xs text-gray-500">{order.orderNo}</div>
              </div>
              <div className="text-right">
                <div className="text-sm mb-1">{order.orderStatusName}</div>
                <div className="text-base font-medium">
                  {formatPrice(order.totalAmount)}
                </div>
              </div>
            </div>

            {/* 주문 상품 목록 */}
            <div className="space-y-4">
              {order.goods.map((goods, index) => (
                <div
                  key={`${goods.goodsNo}-${index}`}
                  className="flex items-center gap-4"
                >
                  <div className="relative w-20 h-20 flex-shrink-0 bg-gray-100">
                    <Image
                      src={goods.goodsMainImageUrl}
                      alt={goods.goodsName}
                      fill
                      className="object-cover"
                    />
                  </div>
                  <div className="flex-1">
                    <h4 className="text-sm font-medium mb-1">{goods.goodsName}</h4>
                    <p className="text-xs text-gray-600">
                      {goods.itemName}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm">{formatPrice(goods.salePrice)}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* 주문 액션 버튼 */}
            <div className="flex gap-2 mt-6">
              <button className="flex-1 py-2.5 border border-gray-300 hover:border-black text-sm transition">
                상세보기
              </button>
              {order.orderStatusCode === "003" && (
                <button className="flex-1 py-2.5 bg-black text-white hover:bg-gray-800 text-sm transition">
                  재주문
                </button>
              )}
              {(order.orderStatusCode === "001" ||
                order.orderStatusCode === "002") && (
                <button className="flex-1 py-2.5 border border-gray-300 hover:border-black text-sm transition">
                  취소
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* 페이징 (더미이므로 비활성화) */}
      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mt-8">
          <button
            onClick={() => setCurrentPage(currentPage - 1)}
            disabled={currentPage === 0}
            className="text-sm disabled:opacity-30 disabled:cursor-not-allowed hover:opacity-60"
          >
            이전
          </button>
          <div className="text-sm text-gray-600">
            {currentPage + 1} / {totalPages}
          </div>
          <button
            onClick={() => setCurrentPage(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
            className="text-sm disabled:opacity-30 disabled:cursor-not-allowed hover:opacity-60"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
