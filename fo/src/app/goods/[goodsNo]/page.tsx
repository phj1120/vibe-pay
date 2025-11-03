"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import Image from "next/image";
import { getGoodsDetail, deleteGoods } from "@/lib/goods-api";
import type { GoodsDetailResponse } from "@/types/goods";
import AlertModal from "@/components/common/AlertModal";
import { useAlert } from "@/hooks/useAlert";

export default function GoodsDetailPage() {
  const params = useParams();
  const router = useRouter();
  const goodsNo = params.goodsNo as string;

  const [goods, setGoods] = useState<GoodsDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const alert = useAlert();

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
    if (!window.confirm("정말 삭제하시겠습니까?")) {
      return;
    }

    try {
      await deleteGoods(goodsNo);
      alert.showAlert("삭제되었습니다");
      router.push("/goods");
    } catch (err) {
      alert.showAlert(err instanceof Error ? err.message : "삭제에 실패했습니다");
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
        <div className="text-sm text-gray-400">로딩 중...</div>
      </div>
    );
  }

  if (error || !goods) {
    return (
      <div className="bg-white min-h-screen">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="text-center py-20">
            <p className="text-gray-400 mb-8">{error ?? "상품을 찾을 수 없습니다"}</p>
            <button
              onClick={() => router.push("/goods")}
              className="px-8 py-3 bg-black text-white text-sm hover:bg-gray-800"
            >
              목록으로
            </button>
          </div>
        </div>
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
      <div className="bg-white min-h-screen">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex justify-between items-center mb-12">
          <h1 className="text-2xl font-medium">상품 상세</h1>
          <div className="flex gap-4">
            <button
              onClick={() => router.push(`/goods/${goodsNo}/edit`)}
              className="text-sm text-gray-600 hover:text-black"
            >
              수정
            </button>
            <button
              onClick={handleDelete}
              className="text-sm text-gray-600 hover:text-black"
            >
              삭제
            </button>
            <button
              onClick={() => router.push("/goods")}
              className="text-sm text-gray-600 hover:text-black"
            >
              목록
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 mb-12">
          {/* 상품 이미지 */}
          <div className="relative w-full aspect-square bg-gray-100">
            <Image
              src={goods.goodsMainImageUrl}
              alt={goods.goodsName}
              fill
              className="object-cover"
            />
          </div>

          {/* 상품 정보 */}
          <div className="space-y-8">
            <div>
              <h2 className="text-2xl font-medium mb-4">{goods.goodsName}</h2>
              <p className="text-3xl font-medium mb-6">{formatPrice(goods.salePrice)}</p>
            </div>

            <div className="border-t pt-6 space-y-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">상품번호</span>
                <span>{goods.goodsNo}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">상품 상태</span>
                <span>{goods.goodsStatusName}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">공급원가</span>
                <span>{formatPrice(goods.supplyPrice)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">등록일시</span>
                <span>{formatDateTime(goods.registDateTime)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">수정일시</span>
                <span>{formatDateTime(goods.modifyDateTime)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* 단품 목록 */}
        <div className="border-t pt-12">
          <h3 className="text-xl font-medium mb-8">단품 목록</h3>
          <div className="space-y-4">
            {goods.items.map((item) => (
              <div
                key={item.itemNo}
                className={`flex items-center justify-between py-6 border-b ${
                  item.isSoldOut ? "bg-gray-50" : ""
                }`}
              >
                <div className="flex-1">
                  <h4 className="font-medium mb-1">{item.itemName}</h4>
                  <p className="text-sm text-gray-600">
                    단품금액: {formatPrice(item.itemPrice)}
                  </p>
                </div>
                <div className="flex items-center gap-8">
                  <div className="text-sm text-gray-600">
                    재고: {item.stock}개
                  </div>
                  <div className="text-sm">
                    <span
                      className={`inline-block px-3 py-1 ${
                        item.isSoldOut
                          ? "bg-gray-200 text-gray-600"
                          : "bg-black text-white"
                      }`}
                    >
                      {item.isSoldOut ? "품절" : item.goodsStatusName}
                    </span>
                  </div>
                  <div className="text-right w-32">
                    <p className="font-medium">
                      {formatPrice(goods.salePrice + item.itemPrice)}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
    </>
  );
}
