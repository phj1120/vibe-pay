"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import Image from "next/image";
import { getGoodsList } from "@/lib/goods-api";
import type { GoodsListItem, GoodsSearchRequest } from "@/types/goods";

export default function GoodsListPage() {
  const router = useRouter();
  const [goods, setGoods] = useState<GoodsListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchParams, setSearchParams] = useState<GoodsSearchRequest>({
    page: 0,
    size: 20,
  });

  useEffect(() => {
    fetchGoods();
  }, [searchParams]);

  async function fetchGoods() {
    try {
      setLoading(true);
      const response = await getGoodsList(searchParams);
      setGoods(response.content);
      setTotalPages(response.totalPages);
      setPage(response.page);
    } catch (err) {
      setError(err instanceof Error ? err.message : "상품 목록을 불러올 수 없습니다");
    } finally {
      setLoading(false);
    }
  }

  function handleSearch(goodsName: string) {
    setSearchParams({
      ...searchParams,
      goodsName: goodsName || undefined,
      page: 0,
    });
  }

  function handlePageChange(newPage: number) {
    setSearchParams({
      ...searchParams,
      page: newPage,
    });
  }

  function formatPrice(price: number): string {
    return price.toLocaleString("ko-KR") + "원";
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col justify-center items-center min-h-screen gap-4">
        <div className="text-lg text-red-600">오류: {error}</div>
        <button
          onClick={() => fetchGoods()}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">상품 목록</h1>
        <Link
          href="/goods/new"
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          상품 등록
        </Link>
      </div>

      {/* 검색 폼 */}
      <div className="mb-6">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const formData = new FormData(e.currentTarget);
            handleSearch(formData.get("goodsName") as string);
          }}
          className="flex gap-2"
        >
          <input
            type="text"
            name="goodsName"
            placeholder="상품명 검색"
            className="flex-1 px-4 py-2 border rounded"
          />
          <button
            type="submit"
            className="px-6 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
          >
            검색
          </button>
        </form>
      </div>

      {/* 상품 그리드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {goods.map((item) => (
          <div
            key={item.goodsNo}
            className="border rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
            onClick={() => router.push(`/goods/${item.goodsNo}`)}
          >
            <div className="relative w-full h-48 bg-gray-200">
              <Image
                src={item.goodsMainImageUrl}
                alt={item.goodsName}
                fill
                className="object-cover"
              />
              {!item.isAvailable && (
                <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                  <span className="text-white text-xl font-bold">품절</span>
                </div>
              )}
            </div>
            <div className="p-4">
              <h3 className="font-semibold text-lg mb-2">{item.goodsName}</h3>
              <div className="text-sm text-gray-600 mb-2">
                {item.goodsStatusName}
              </div>
              <div className="flex justify-between items-center">
                <div>
                  <div className="text-xl font-bold text-blue-600">
                    {formatPrice(item.salePrice + item.minItemPrice)}
                  </div>
                  {item.minItemPrice !== item.maxItemPrice && (
                    <div className="text-sm text-gray-500">
                      ~ {formatPrice(item.salePrice + item.maxItemPrice)}
                    </div>
                  )}
                </div>
                <div className="text-sm text-gray-600">
                  재고: {item.totalStock}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-8">
          <button
            onClick={() => handlePageChange(page - 1)}
            disabled={page === 0}
            className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed"
          >
            이전
          </button>
          <span className="px-4 py-2">
            {page + 1} / {totalPages}
          </span>
          <button
            onClick={() => handlePageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}
