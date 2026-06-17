"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { registerGoods } from "@/lib/goods-api";
import type { GoodsRegisterRequest, GoodsItem } from "@/types/goods";
import AlertModal from "@/components/common/AlertModal";
import { useAlert } from "@/hooks/useAlert";

type NumberInputValue = number | "";
type GoodsItemForm = Omit<GoodsItem, "itemPrice" | "stock"> & {
  itemPrice: NumberInputValue;
  stock: NumberInputValue;
};
type GoodsRegisterForm = Omit<GoodsRegisterRequest, "salePrice" | "supplyPrice" | "items"> & {
  salePrice: NumberInputValue;
  supplyPrice: NumberInputValue;
  items: GoodsItemForm[];
};

function parseNumberInput(value: string): NumberInputValue {
  return value === "" ? "" : Number(value);
}

function toNumber(value: NumberInputValue): number {
  return value === "" ? 0 : value;
}

export default function GoodsRegisterPage() {
  const router = useRouter();
  const alert = useAlert();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<GoodsRegisterForm>({
    goodsName: "",
    goodsStatusCode: "001",
    goodsMainImageUrl: "",
    salePrice: "",
    supplyPrice: "",
    items: [
      {
        itemName: "",
        itemPrice: "",
        stock: "",
        goodsStatusCode: "001",
      },
    ],
  });

  function addItem() {
    setFormData({
      ...formData,
      items: [
        ...formData.items,
        {
          itemName: "",
          itemPrice: "",
          stock: "",
          goodsStatusCode: "001",
        },
      ],
    });
  }

  function removeItem(index: number) {
    const newItems = formData.items.filter((_, i) => i !== index);
    setFormData({
      ...formData,
      items: newItems,
    });
  }

  function updateItem(index: number, field: keyof GoodsItemForm, value: string | number) {
    const newItems = [...formData.items];
    newItems[index] = {
      ...newItems[index],
      [field]: value,
    };
    setFormData({
      ...formData,
      items: newItems,
    });
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    // 유효성 검사
    if (!formData.goodsName.trim()) {
      alert.showAlert("상품명을 입력해주세요");
      return;
    }
    if (!formData.goodsMainImageUrl.trim()) {
      alert.showAlert("상품 이미지 URL을 입력해주세요");
      return;
    }
    if (formData.salePrice === "" || formData.salePrice <= 0) {
      alert.showAlert("판매가는 0보다 커야 합니다");
      return;
    }
    if (formData.supplyPrice === "" || formData.supplyPrice <= 0) {
      alert.showAlert("공급원가는 0보다 커야 합니다");
      return;
    }
    if (formData.items.length === 0) {
      alert.showAlert("단품을 최소 1개 이상 추가해주세요");
      return;
    }

    for (let i = 0; i < formData.items.length; i++) {
      const item = formData.items[i];
      if (!item.itemName.trim()) {
        alert.showAlert(`${i + 1}번째 단품의 이름을 입력해주세요`);
        return;
      }
      if (item.itemPrice === "" || item.itemPrice < 0) {
        alert.showAlert(`${i + 1}번째 단품 금액은 0 이상이어야 합니다`);
        return;
      }
      if (item.stock === "" || item.stock < 0) {
        alert.showAlert(`${i + 1}번째 단품의 재고는 0 이상이어야 합니다`);
        return;
      }
    }

    try {
      setLoading(true);
      const payload: GoodsRegisterRequest = {
        ...formData,
        salePrice: formData.salePrice,
        supplyPrice: formData.supplyPrice,
        items: formData.items.map((item) => ({
          ...item,
          itemPrice: toNumber(item.itemPrice),
          stock: toNumber(item.stock),
        })),
      };
      const goodsNo = await registerGoods(payload);
      alert.showAlert("상품이 등록되었습니다");
      router.push(`/goods/${goodsNo}`);
    } catch (err) {
      alert.showAlert(err instanceof Error ? err.message : "상품 등록에 실패했습니다");
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <AlertModal isOpen={alert.isOpen} message={alert.message} onClose={alert.hideAlert} />
      <div className="bg-white min-h-screen">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-2xl font-medium mb-12">상품 등록</h1>

        <form onSubmit={handleSubmit} className="max-w-2xl">
          {/* 상품 기본 정보 */}
          <div className="space-y-4 mb-8">
            <h2 className="text-lg font-medium mb-4">기본 정보</h2>

            <div>
              <input
                type="text"
                value={formData.goodsName}
                onChange={(e) => setFormData({ ...formData, goodsName: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm"
                placeholder="상품명"
                required
              />
            </div>

            <div>
              <select
                value={formData.goodsStatusCode}
                onChange={(e) => setFormData({ ...formData, goodsStatusCode: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm"
                required
              >
                <option value="001">판매중</option>
                <option value="002">판매중단</option>
                <option value="003">품절</option>
              </select>
            </div>

            <div>
              <input
                type="url"
                value={formData.goodsMainImageUrl}
                onChange={(e) => setFormData({ ...formData, goodsMainImageUrl: e.target.value })}
                className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm"
                placeholder="상품 이미지 URL"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <input
                  type="number"
                  value={formData.salePrice}
                  onChange={(e) =>
                    setFormData({ ...formData, salePrice: parseNumberInput(e.target.value) })
                  }
                  className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm"
                  placeholder="판매가"
                  min="0"
                  required
                />
              </div>
              <div>
                <input
                  type="number"
                  value={formData.supplyPrice}
                  onChange={(e) =>
                    setFormData({ ...formData, supplyPrice: parseNumberInput(e.target.value) })
                  }
                  className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm"
                  placeholder="공급원가"
                  min="0"
                  required
                />
              </div>
            </div>
          </div>

          {/* 단품 목록 */}
          <div className="space-y-4 mb-8">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-medium">단품 목록</h2>
              <button
                type="button"
                onClick={addItem}
                className="px-4 py-2 bg-black text-white text-sm hover:bg-gray-800"
              >
                단품 추가
              </button>
            </div>

            {formData.items.map((item, index) => (
              <div key={index} className="border border-gray-300 p-4 space-y-3">
                <div className="flex justify-between items-center mb-2">
                  <h3 className="text-sm font-medium text-gray-900">단품 #{index + 1}</h3>
                  {formData.items.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeItem(index)}
                      className="text-sm text-gray-600 hover:text-black"
                    >
                      삭제
                    </button>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <input
                      type="text"
                      value={item.itemName}
                      onChange={(e) => updateItem(index, "itemName", e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                      placeholder="단품명"
                      required
                    />
                  </div>
                  <div>
                    <select
                      value={item.goodsStatusCode}
                      onChange={(e) => updateItem(index, "goodsStatusCode", e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                      required
                    >
                      <option value="001">판매중</option>
                      <option value="002">판매중단</option>
                      <option value="003">품절</option>
                    </select>
                  </div>
                  <div>
                    <input
                      type="number"
                      value={item.itemPrice}
                      onChange={(e) =>
                        updateItem(index, "itemPrice", parseNumberInput(e.target.value))
                      }
                      className="w-full px-3 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                      placeholder="단품 금액"
                      min="0"
                      required
                    />
                  </div>
                  <div>
                    <input
                      type="number"
                      value={item.stock}
                      onChange={(e) => updateItem(index, "stock", parseNumberInput(e.target.value))}
                      className="w-full px-3 py-2 border border-gray-300 focus:outline-none focus:border-black text-sm"
                      placeholder="재고"
                      min="0"
                      required
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* 버튼 */}
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => router.push("/")}
              className="flex-1 py-3 border border-gray-300 text-sm hover:bg-gray-50 disabled:opacity-50"
              disabled={loading}
            >
              취소
            </button>
            <button
              type="submit"
              className="flex-1 bg-black text-white py-3 text-sm hover:bg-gray-800 disabled:bg-gray-300 disabled:cursor-not-allowed"
              disabled={loading}
            >
              {loading ? "등록 중..." : "등록"}
            </button>
          </div>
        </form>
        </div>
      </div>
    </>
  );
}
