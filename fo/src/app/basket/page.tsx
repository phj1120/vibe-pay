"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import {
  getBasketList,
  modifyBasket,
  deleteBasket,
  deleteBaskets,
  deleteAllBaskets,
} from "@/lib/basket-api";
import { ApiError } from "@/lib/api-client";
import type { BasketItem } from "@/types/basket";

export default function BasketPage() {
  const router = useRouter();
  const [basketItems, setBasketItems] = useState<BasketItem[]>([]);
  const [selectedItems, setSelectedItems] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>("");

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      alert("로그인이 필요한 서비스입니다");
      router.push("/login");
      return;
    }

    fetchBasketList();
  }, [router]);

  async function fetchBasketList() {
    try {
      setLoading(true);
      const items = await getBasketList();
      setBasketItems(items);
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
        setError("장바구니를 불러오는 중 오류가 발생했습니다");
      }
    } finally {
      setLoading(false);
    }
  }

  function formatPrice(price: number): string {
    return price.toLocaleString("ko-KR") + "원";
  }

  function handleSelectAll() {
    if (selectedItems.size === basketItems.length) {
      setSelectedItems(new Set());
    } else {
      setSelectedItems(new Set(basketItems.map((item) => item.basketNo)));
    }
  }

  function handleSelectItem(basketNo: string) {
    const newSelected = new Set(selectedItems);
    if (newSelected.has(basketNo)) {
      newSelected.delete(basketNo);
    } else {
      newSelected.add(basketNo);
    }
    setSelectedItems(newSelected);
  }

  async function handleQuantityChange(basketNo: string, newQuantity: number) {
    if (newQuantity < 1) return;

    try {
      await modifyBasket(basketNo, { quantity: newQuantity });
      await fetchBasketList();
    } catch (err) {
      if (err instanceof ApiError) {
        alert(err.message);
      } else {
        alert("수량 변경 중 오류가 발생했습니다");
      }
    }
  }

  async function handleDeleteItem(basketNo: string) {
    if (!confirm("해당 상품을 장바구니에서 삭제하시겠습니까?")) return;

    try {
      await deleteBasket(basketNo);
      await fetchBasketList();
      setSelectedItems((prev) => {
        const newSet = new Set(prev);
        newSet.delete(basketNo);
        return newSet;
      });
    } catch (err) {
      if (err instanceof ApiError) {
        alert(err.message);
      } else {
        alert("삭제 중 오류가 발생했습니다");
      }
    }
  }

  async function handleDeleteSelected() {
    if (selectedItems.size === 0) {
      alert("삭제할 상품을 선택해주세요");
      return;
    }

    if (!confirm(`선택한 ${selectedItems.size}개 상품을 삭제하시겠습니까?`)) return;

    try {
      await deleteBaskets(Array.from(selectedItems));
      await fetchBasketList();
      setSelectedItems(new Set());
    } catch (err) {
      if (err instanceof ApiError) {
        alert(err.message);
      } else {
        alert("삭제 중 오류가 발생했습니다");
      }
    }
  }

  async function handleDeleteAll() {
    if (basketItems.length === 0) return;

    if (!confirm("장바구니의 모든 상품을 삭제하시겠습니까?")) return;

    try {
      await deleteAllBaskets();
      await fetchBasketList();
      setSelectedItems(new Set());
    } catch (err) {
      if (err instanceof ApiError) {
        alert(err.message);
      } else {
        alert("삭제 중 오류가 발생했습니다");
      }
    }
  }

  function handleOrder() {
    if (selectedItems.size === 0) {
      alert("주문할 상품을 선택해주세요");
      return;
    }

    const selectedBasketNos = Array.from(selectedItems).join(",");
    router.push(`/order?basketNos=${selectedBasketNos}`);
  }

  const totalPrice = basketItems
    .filter((item) => selectedItems.has(item.basketNo))
    .reduce((sum, item) => sum + item.salePrice * item.quantity, 0);

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-sm text-gray-400">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-sm text-gray-900">{error}</div>
      </div>
    );
  }

  return (
    <div className="bg-white min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-2xl font-medium mb-12">장바구니</h1>

        {basketItems.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-400 mb-8">장바구니가 비어있습니다</p>
            <button
              onClick={() => router.push("/")}
              className="px-8 py-3 bg-black text-white text-sm hover:bg-gray-800"
            >
              쇼핑 계속하기
            </button>
          </div>
        ) : (
          <>
            {/* 상단 컨트롤 */}
            <div className="flex items-center justify-between border-b pb-4 mb-8">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={selectedItems.size === basketItems.length}
                  onChange={handleSelectAll}
                  className="w-4 h-4"
                />
                <span className="text-sm">전체선택 ({selectedItems.size}/{basketItems.length})</span>
              </label>
              <div className="flex gap-4">
                <button
                  onClick={handleDeleteSelected}
                  className="text-sm text-gray-600 hover:text-black"
                >
                  선택삭제
                </button>
                <button
                  onClick={handleDeleteAll}
                  className="text-sm text-gray-600 hover:text-black"
                >
                  전체삭제
                </button>
              </div>
            </div>

            {/* 장바구니 아이템 목록 */}
            <div className="space-y-6 mb-12">
              {basketItems.map((item) => (
                <div
                  key={item.basketNo}
                  className="flex items-center gap-6 py-6 border-b"
                >
                  <input
                    type="checkbox"
                    checked={selectedItems.has(item.basketNo)}
                    onChange={() => handleSelectItem(item.basketNo)}
                    className="w-4 h-4"
                  />

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
                    <p className="text-sm text-gray-900 mt-1">{formatPrice(item.salePrice)}</p>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleQuantityChange(item.basketNo, item.quantity - 1)}
                      className="w-8 h-8 border border-gray-300 hover:bg-gray-50"
                    >
                      -
                    </button>
                    <span className="w-12 text-center text-sm">{item.quantity}</span>
                    <button
                      onClick={() => handleQuantityChange(item.basketNo, item.quantity + 1)}
                      className="w-8 h-8 border border-gray-300 hover:bg-gray-50"
                    >
                      +
                    </button>
                  </div>

                  <div className="text-right w-32">
                    <p className="font-medium mt-1">
                      {formatPrice(item.salePrice * item.quantity)}
                    </p>
                  </div>

                  <button
                    onClick={() => handleDeleteItem(item.basketNo)}
                    className="text-gray-400 hover:text-black text-xl"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>

            {/* 주문 요약 */}
            <div className="border-t pt-8">
              <div className="space-y-3 mb-6">
                <div className="flex justify-between items-center">
                  <span className="text-base">총 금액</span>
                  <span className="text-2xl font-medium">
                    {formatPrice(totalPrice)}
                  </span>
                </div>
              </div>
              <button
                onClick={handleOrder}
                className="w-full py-4 bg-black text-white text-sm hover:bg-gray-800"
              >
                주문하기
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
