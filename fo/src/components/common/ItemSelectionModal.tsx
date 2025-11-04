"use client";

import { useState } from "react";
import type { GoodsItem } from "@/types/goods";

interface ItemSelectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelect: (items: Array<{ itemNo: string; quantity: number }>) => void;
  items: GoodsItem[];
  goodsName: string;
}

interface SelectedItem {
  itemNo: string;
  quantity: number;
}

export default function ItemSelectionModal({
  isOpen,
  onClose,
  onSelect,
  items,
  goodsName,
}: ItemSelectionModalProps) {
  const [selectedItems, setSelectedItems] = useState<Map<string, number>>(new Map());

  if (!isOpen) return null;

  const handleConfirm = () => {
    if (selectedItems.size === 0) {
      alert("단품을 선택해주세요");
      return;
    }
    const itemsArray = Array.from(selectedItems.entries()).map(([itemNo, quantity]) => ({
      itemNo,
      quantity,
    }));
    onSelect(itemsArray);
    handleClose();
  };

  const handleClose = () => {
    setSelectedItems(new Map());
    onClose();
  };

  const formatPrice = (price: number): string => {
    return price.toLocaleString("ko-KR") + "원";
  };

  const handleItemToggle = (itemNo: string) => {
    const newSelectedItems = new Map(selectedItems);
    if (newSelectedItems.has(itemNo)) {
      newSelectedItems.delete(itemNo);
    } else {
      newSelectedItems.set(itemNo, 1);
    }
    setSelectedItems(newSelectedItems);
  };

  const handleQuantityChange = (itemNo: string, newQuantity: number, maxStock: number) => {
    const quantity = Math.max(1, Math.min(maxStock, newQuantity));
    const newSelectedItems = new Map(selectedItems);
    newSelectedItems.set(itemNo, quantity);
    setSelectedItems(newSelectedItems);
  };

  const availableItems = items.filter((item) => item.stock > 0 && !item.isSoldOut);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 배경 오버레이 */}
      <div
        className="absolute inset-0 bg-black bg-opacity-50"
        onClick={handleClose}
      />

      {/* 모달 콘텐츠 */}
      <div className="relative bg-white w-full max-w-md mx-4 rounded-lg shadow-xl">
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 py-4 border-b">
          <h2 className="text-lg font-semibold text-gray-900">단품 선택</h2>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* 본문 */}
        <div className="px-6 py-4">
          <div className="mb-4">
            <p className="text-sm text-gray-600 mb-4">{goodsName}</p>
          </div>

          {availableItems.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              선택 가능한 단품이 없습니다
            </div>
          ) : (
            <>
              {/* 단품 선택 */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-3">
                  단품 선택 * (복수 선택 가능)
                </label>
                <div className="space-y-3 max-h-96 overflow-y-auto">
                  {availableItems.map((item) => {
                    const isSelected = selectedItems.has(item.itemNo || "");
                    const quantity = selectedItems.get(item.itemNo || "") || 1;
                    return (
                      <div
                        key={item.itemNo}
                        className={`border rounded-lg p-4 transition ${
                          isSelected
                            ? "border-black bg-gray-50"
                            : "border-gray-300 hover:border-gray-400"
                        }`}
                      >
                        {/* 단품 정보 */}
                        <label className="flex items-start cursor-pointer">
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={() => handleItemToggle(item.itemNo || "")}
                            className="w-4 h-4 mt-0.5 text-black focus:ring-black rounded"
                          />
                          <div className="ml-3 flex-1">
                            <div className="flex items-start justify-between">
                              <span className="text-sm font-medium text-gray-900">
                                {item.itemName}
                              </span>
                              <span className="text-sm text-gray-900 font-medium ml-2">
                                {formatPrice(item.itemPrice)}
                              </span>
                            </div>
                            <span className="text-xs text-gray-500 mt-1 block">
                              재고: {item.stock}개
                            </span>
                          </div>
                        </label>

                        {/* 수량 선택 (선택된 경우에만 표시) */}
                        {isSelected && (
                          <div className="mt-3 pt-3 border-t border-gray-200">
                            <div className="flex items-center justify-between">
                              <span className="text-xs text-gray-600">수량</span>
                              <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden">
                                <button
                                  type="button"
                                  onClick={() =>
                                    handleQuantityChange(
                                      item.itemNo || "",
                                      quantity - 1,
                                      item.stock
                                    )
                                  }
                                  className="w-8 h-8 flex items-center justify-center text-gray-600 hover:bg-gray-100 transition"
                                >
                                  <span className="text-lg leading-none">−</span>
                                </button>
                                <input
                                  type="number"
                                  value={quantity}
                                  onChange={(e) =>
                                    handleQuantityChange(
                                      item.itemNo || "",
                                      parseInt(e.target.value) || 1,
                                      item.stock
                                    )
                                  }
                                  className="w-12 h-8 text-center border-none focus:outline-none text-sm"
                                  min="1"
                                  max={item.stock}
                                />
                                <button
                                  type="button"
                                  onClick={() =>
                                    handleQuantityChange(
                                      item.itemNo || "",
                                      quantity + 1,
                                      item.stock
                                    )
                                  }
                                  className="w-8 h-8 flex items-center justify-center text-gray-600 hover:bg-gray-100 transition"
                                >
                                  <span className="text-lg leading-none">+</span>
                                </button>
                              </div>
                            </div>
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            </>
          )}
        </div>

        {/* 푸터 */}
        <div className="flex gap-3 px-6 py-4 border-t">
          <button
            onClick={handleClose}
            className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 hover:bg-gray-50 transition"
          >
            취소
          </button>
          <button
            onClick={handleConfirm}
            disabled={selectedItems.size === 0 || availableItems.length === 0}
            className="flex-1 px-4 py-3 bg-black text-white hover:bg-gray-800 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
          >
            장바구니에 담기 ({selectedItems.size})
          </button>
        </div>
      </div>
    </div>
  );
}

