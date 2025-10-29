"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { getGoodsList, getGoodsDetail } from "@/lib/goods-api";
import type { GoodsListItem, GoodsSearchRequest } from "@/types/goods";
import { useBasketStore } from "@/store/basket-store";
import { useToast } from "@/hooks/useToast";
import Toast from "@/components/ui/Toast";

export default function Home() {
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
  const [addingToBasket, setAddingToBasket] = useState<string | null>(null);

  const { addBasket } = useBasketStore();
  const { toasts, removeToast, success, error: showError } = useToast();

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

  async function handleAddToBasket(
    event: React.MouseEvent,
    goodsNo: string,
    goodsName: string,
    isAvailable: boolean
  ) {
    event.stopPropagation();

    if (!isAvailable) {
      showError("품절된 상품입니다");
      return;
    }

    try {
      setAddingToBasket(goodsNo);

      // 상품 상세 정보 조회하여 단품 정보 가져오기
      const goodsDetail = await getGoodsDetail(goodsNo);

      if (!goodsDetail.items || goodsDetail.items.length === 0) {
        showError("상품 정보를 찾을 수 없습니다");
        return;
      }

      // 재고가 있는 첫 번째 단품 찾기
      const availableItem = goodsDetail.items.find(
        (item) => item.stock > 0 && !item.isSoldOut
      );

      if (!availableItem || !availableItem.itemNo) {
        showError("재고가 없습니다");
        return;
      }

      // 장바구니에 추가
      await addBasket({
        goodsNo,
        itemNo: availableItem.itemNo,
        quantity: 1,
      });

      success(`${goodsName}을(를) 장바구니에 담았습니다`);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "장바구니 담기에 실패했습니다";
      showError(errorMessage);
    } finally {
      setAddingToBasket(null);
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[calc(100vh-200px)]">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[calc(100vh-200px)] gap-4">
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
    <>
      <div className="bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 상단 배너 */}
        <div className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-lg p-8 mb-8 text-white">
          <h1 className="text-3xl font-bold mb-2">Vibe Pay 쇼핑몰에 오신 것을 환영합니다</h1>
          <p className="text-blue-100">간편하고 안전한 쇼핑 경험을 제공합니다</p>
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
              className="flex-1 px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              type="submit"
              className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              검색
            </button>
          </form>
        </div>

        {/* 상품 그리드 */}
        {goods.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            등록된 상품이 없습니다
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {goods.map((item) => (
                <div
                  key={item.goodsNo}
                  className="bg-white border rounded-lg overflow-hidden hover:shadow-lg transition-shadow"
                >
                  <div
                    className="cursor-pointer"
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
                  <div className="px-4 pb-4">
                    <button
                      onClick={(e) =>
                        handleAddToBasket(e, item.goodsNo, item.goodsName, item.isAvailable)
                      }
                      disabled={!item.isAvailable || addingToBasket === item.goodsNo}
                      className="w-full py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
                    >
                      {addingToBasket === item.goodsNo
                        ? "담는 중..."
                        : "장바구니 담기"}
                    </button>
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
                  className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                >
                  이전
                </button>
                <span className="px-4 py-2">
                  {page + 1} / {totalPages}
                </span>
                <button
                  onClick={() => handlePageChange(page + 1)}
                  disabled={page >= totalPages - 1}
                  className="px-4 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
                >
                  다음
                </button>
              </div>
            )}
          </>
        )}
        </div>
      </div>

      {/* 토스트 알림 */}
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          message={toast.message}
          type={toast.type}
          onClose={() => removeToast(toast.id)}
        />
      ))}
    </>
  );
}
