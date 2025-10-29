"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { getGoodsDetail, modifyGoods } from "@/lib/goods-api";
import type { GoodsModifyRequest, GoodsItem } from "@/types/goods";

export default function GoodsEditPage() {
  const params = useParams();
  const router = useRouter();
  const goodsNo = params.goodsNo as string;

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState<GoodsModifyRequest>({
    goodsName: "",
    goodsStatusCode: "001",
    goodsMainImageUrl: "",
    salePrice: 0,
    supplyPrice: 0,
    items: [],
  });

  useEffect(() => {
    fetchGoodsDetail();
  }, [goodsNo]);

  async function fetchGoodsDetail() {
    try {
      setLoading(true);
      const data = await getGoodsDetail(goodsNo);
      setFormData({
        goodsName: data.goodsName,
        goodsStatusCode: data.goodsStatusCode,
        goodsMainImageUrl: data.goodsMainImageUrl,
        salePrice: data.salePrice,
        supplyPrice: data.supplyPrice,
        items: data.items.map((item) => ({
          itemName: item.itemName,
          itemPrice: item.itemPrice,
          stock: item.stock,
          goodsStatusCode: item.goodsStatusCode,
        })),
      });
    } catch (err) {
      alert(err instanceof Error ? err.message : "상품 정보를 불러올 수 없습니다");
      router.push("/goods");
    } finally {
      setLoading(false);
    }
  }

  function addItem() {
    setFormData({
      ...formData,
      items: [
        ...formData.items,
        {
          itemName: "",
          itemPrice: 0,
          stock: 0,
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

  function updateItem(index: number, field: keyof GoodsItem, value: string | number) {
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
      alert("상품명을 입력해주세요");
      return;
    }
    if (!formData.goodsMainImageUrl.trim()) {
      alert("상품 이미지 URL을 입력해주세요");
      return;
    }
    if (formData.salePrice <= 0) {
      alert("판매가는 0보다 커야 합니다");
      return;
    }
    if (formData.supplyPrice <= 0) {
      alert("공급원가는 0보다 커야 합니다");
      return;
    }
    if (formData.items.length === 0) {
      alert("단품을 최소 1개 이상 추가해주세요");
      return;
    }

    for (let i = 0; i < formData.items.length; i++) {
      const item = formData.items[i];
      if (!item.itemName.trim()) {
        alert(`${i + 1}번째 단품의 이름을 입력해주세요`);
        return;
      }
      if (item.stock < 0) {
        alert(`${i + 1}번째 단품의 재고는 0 이상이어야 합니다`);
        return;
      }
    }

    try {
      setSubmitting(true);
      await modifyGoods(goodsNo, formData);
      alert("상품이 수정되었습니다");
      router.push(`/goods/${goodsNo}`);
    } catch (err) {
      alert(err instanceof Error ? err.message : "상품 수정에 실패했습니다");
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-3xl font-bold mb-6">상품 수정</h1>

      <form onSubmit={handleSubmit} className="bg-white shadow rounded-lg p-6">
        {/* 상품 기본 정보 */}
        <div className="space-y-4 mb-8">
          <h2 className="text-xl font-bold">기본 정보</h2>

          <div>
            <label className="block text-sm font-medium mb-1">상품명 *</label>
            <input
              type="text"
              value={formData.goodsName}
              onChange={(e) => setFormData({ ...formData, goodsName: e.target.value })}
              className="w-full px-4 py-2 border rounded"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">상품 상태 *</label>
            <select
              value={formData.goodsStatusCode}
              onChange={(e) => setFormData({ ...formData, goodsStatusCode: e.target.value })}
              className="w-full px-4 py-2 border rounded"
              required
            >
              <option value="001">판매중</option>
              <option value="002">판매중단</option>
              <option value="003">품절</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">상품 이미지 URL *</label>
            <input
              type="url"
              value={formData.goodsMainImageUrl}
              onChange={(e) => setFormData({ ...formData, goodsMainImageUrl: e.target.value })}
              className="w-full px-4 py-2 border rounded"
              placeholder="https://example.com/image.jpg"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-1">판매가 *</label>
              <input
                type="number"
                value={formData.salePrice}
                onChange={(e) =>
                  setFormData({ ...formData, salePrice: parseInt(e.target.value) || 0 })
                }
                className="w-full px-4 py-2 border rounded"
                min="0"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">공급원가 *</label>
              <input
                type="number"
                value={formData.supplyPrice}
                onChange={(e) =>
                  setFormData({ ...formData, supplyPrice: parseInt(e.target.value) || 0 })
                }
                className="w-full px-4 py-2 border rounded"
                min="0"
                required
              />
            </div>
          </div>
        </div>

        {/* 단품 목록 */}
        <div className="space-y-4 mb-6">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-bold">단품 목록</h2>
            <button
              type="button"
              onClick={addItem}
              className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
            >
              단품 추가
            </button>
          </div>

          {formData.items.map((item, index) => (
            <div key={index} className="border rounded p-4 space-y-3">
              <div className="flex justify-between items-center mb-2">
                <h3 className="font-medium">단품 #{index + 1}</h3>
                {formData.items.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removeItem(index)}
                    className="text-red-500 hover:text-red-700"
                  >
                    삭제
                  </button>
                )}
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium mb-1">단품명 *</label>
                  <input
                    type="text"
                    value={item.itemName}
                    onChange={(e) => updateItem(index, "itemName", e.target.value)}
                    className="w-full px-3 py-2 border rounded"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">단품 상태 *</label>
                  <select
                    value={item.goodsStatusCode}
                    onChange={(e) => updateItem(index, "goodsStatusCode", e.target.value)}
                    className="w-full px-3 py-2 border rounded"
                    required
                  >
                    <option value="001">판매중</option>
                    <option value="002">판매중단</option>
                    <option value="003">품절</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">단품 금액 *</label>
                  <input
                    type="number"
                    value={item.itemPrice}
                    onChange={(e) =>
                      updateItem(index, "itemPrice", parseInt(e.target.value) || 0)
                    }
                    className="w-full px-3 py-2 border rounded"
                    min="0"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">재고 *</label>
                  <input
                    type="number"
                    value={item.stock}
                    onChange={(e) => updateItem(index, "stock", parseInt(e.target.value) || 0)}
                    className="w-full px-3 py-2 border rounded"
                    min="0"
                    required
                  />
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 버튼 */}
        <div className="flex gap-2 justify-end">
          <button
            type="button"
            onClick={() => router.push(`/goods/${goodsNo}`)}
            className="px-6 py-2 border rounded hover:bg-gray-50"
            disabled={submitting}
          >
            취소
          </button>
          <button
            type="submit"
            className="px-6 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
            disabled={submitting}
          >
            {submitting ? "수정 중..." : "수정"}
          </button>
        </div>
      </form>
    </div>
  );
}
