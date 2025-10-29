"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import Image from "next/image";
import { getGoodsDetail, deleteGoods } from "@/lib/goods-api";
import type { GoodsDetailResponse } from "@/types/goods";

export default function GoodsDetailPage() {
  const params = useParams();
  const router = useRouter();
  const goodsNo = params.goodsNo as string;

  const [goods, setGoods] = useState<GoodsDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchGoodsDetail();
  }, [goodsNo]);

  async function fetchGoodsDetail() {
    try {
      setLoading(true);
      const data = await getGoodsDetail(goodsNo);
      setGoods(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "상품 정보를 불러올 수 없습니다");
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete() {
    if (!confirm("정말 삭제하시겠습니까?")) {
      return;
    }

    try {
      await deleteGoods(goodsNo);
      alert("삭제되었습니다");
      router.push("/goods");
    } catch (err) {
      alert(err instanceof Error ? err.message : "삭제에 실패했습니다");
    }
  }

  function formatPrice(price: number): string {
    return price.toLocaleString("ko-KR") + "원";
  }

  function formatDateTime(dateTime: string): string {
    return new Date(dateTime).toLocaleString("ko-KR");
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  if (error || !goods) {
    return (
      <div className="flex flex-col justify-center items-center min-h-screen gap-4">
        <div className="text-lg text-red-600">오류: {error ?? "상품을 찾을 수 없습니다"}</div>
        <button
          onClick={() => router.push("/goods")}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          목록으로
        </button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">상품 상세</h1>
        <div className="flex gap-2">
          <button
            onClick={() => router.push(`/goods/${goodsNo}/edit`)}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            수정
          </button>
          <button
            onClick={handleDelete}
            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
          >
            삭제
          </button>
          <button
            onClick={() => router.push("/goods")}
            className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
          >
            목록
          </button>
        </div>
      </div>

      <div className="bg-white shadow rounded-lg overflow-hidden">
        {/* 상품 이미지 */}
        <div className="relative w-full h-96 bg-gray-200">
          <Image
            src={goods.goodsMainImageUrl}
            alt={goods.goodsName}
            fill
            className="object-cover"
          />
        </div>

        {/* 상품 정보 */}
        <div className="p-6">
          <h2 className="text-2xl font-bold mb-4">{goods.goodsName}</h2>

          <div className="grid grid-cols-2 gap-4 mb-6">
            <div>
              <div className="text-sm text-gray-600 mb-1">상품 상태</div>
              <div className="font-medium">{goods.goodsStatusName}</div>
            </div>
            <div>
              <div className="text-sm text-gray-600 mb-1">상품번호</div>
              <div className="font-medium">{goods.goodsNo}</div>
            </div>
            <div>
              <div className="text-sm text-gray-600 mb-1">판매가</div>
              <div className="text-xl font-bold text-blue-600">
                {formatPrice(goods.salePrice)}
              </div>
            </div>
            <div>
              <div className="text-sm text-gray-600 mb-1">공급원가</div>
              <div className="font-medium">{formatPrice(goods.supplyPrice)}</div>
            </div>
            <div>
              <div className="text-sm text-gray-600 mb-1">등록일시</div>
              <div className="font-medium">{formatDateTime(goods.registDateTime)}</div>
            </div>
            <div>
              <div className="text-sm text-gray-600 mb-1">수정일시</div>
              <div className="font-medium">{formatDateTime(goods.modifyDateTime)}</div>
            </div>
          </div>

          {/* 단품 목록 */}
          <div>
            <h3 className="text-xl font-bold mb-4">단품 목록</h3>
            <div className="border rounded-lg overflow-hidden">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">
                      단품명
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">
                      단품금액
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">
                      재고
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">
                      상태
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">
                      총 가격
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {goods.items.map((item) => (
                    <tr key={item.itemNo} className={item.isSoldOut ? "bg-gray-100" : ""}>
                      <td className="px-4 py-3">{item.itemName}</td>
                      <td className="px-4 py-3">{formatPrice(item.itemPrice)}</td>
                      <td className="px-4 py-3">{item.stock}</td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-block px-2 py-1 text-xs rounded ${
                            item.isSoldOut
                              ? "bg-red-100 text-red-800"
                              : "bg-green-100 text-green-800"
                          }`}
                        >
                          {item.isSoldOut ? "품절" : item.goodsStatusName}
                        </span>
                      </td>
                      <td className="px-4 py-3 font-bold text-blue-600">
                        {formatPrice(goods.salePrice + item.itemPrice)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
