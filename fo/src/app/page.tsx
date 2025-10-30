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
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-sm text-gray-400">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col justify-center items-center min-h-screen gap-4">
        <div className="text-sm text-gray-900">{error}</div>
        <button
          onClick={() => fetchGoods()}
          className="px-6 py-2 bg-black text-white text-sm hover:bg-gray-800"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <>
      <div className="bg-white min-h-screen">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* 검색 폼 */}
        <div className="mb-12">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              const formData = new FormData(e.currentTarget);
              handleSearch(formData.get("goodsName") as string);
            }}
            className="flex gap-2 max-w-xl"
          >
            <input
              type="text"
              name="goodsName"
              placeholder="상품 검색"
              className="flex-1 px-4 py-3 border border-gray-300 focus:outline-none focus:border-black transition"
            />
            <button
              type="submit"
              className="px-8 py-3 bg-black text-white hover:bg-gray-800 transition"
            >
              검색
            </button>
          </form>
        </div>

        {/* 상품 그리드 */}
        {goods.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            등록된 상품이 없습니다
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
              {goods.map((item) => (
                <div
                  key={item.goodsNo}
                  className="group"
                >
                  <div
                    className="cursor-pointer"
                    onClick={() => router.push(`/goods/${item.goodsNo}`)}
                  >
                    <div className="relative w-full aspect-square bg-gray-100 mb-4 overflow-hidden">
                      <Image
                        src={item.goodsMainImageUrl}
                        alt={item.goodsName}
                        fill
                        className="object-cover group-hover:scale-105 transition-transform duration-300"
                      />
                      {!item.isAvailable && (
                        <div className="absolute inset-0 bg-black bg-opacity-60 flex items-center justify-center">
                          <span className="text-white text-sm font-medium">SOLD OUT</span>
                        </div>
                      )}
                    </div>
                    <div>
                      <h3 className="font-medium text-base mb-2 text-gray-900">{item.goodsName}</h3>
                      <div className="text-sm text-gray-900 font-medium">
                        {formatPrice(item.salePrice + item.minItemPrice)}
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={(e) =>
                      handleAddToBasket(e, item.goodsNo, item.goodsName, item.isAvailable)
                    }
                    disabled={!item.isAvailable || addingToBasket === item.goodsNo}
                    className="w-full mt-3 py-2.5 bg-black text-white text-sm hover:bg-gray-800 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
                  >
                    {addingToBasket === item.goodsNo
                      ? "담는 중..."
                      : "장바구니"}
                  </button>
                </div>
              ))}
            </div>

            {/* 페이지네이션 */}
            {totalPages > 1 && (
              <div className="flex justify-center items-center gap-4 mt-16">
                <button
                  onClick={() => handlePageChange(page - 1)}
                  disabled={page === 0}
                  className="px-4 py-2 text-sm disabled:opacity-30 disabled:cursor-not-allowed hover:opacity-60 transition"
                >
                  이전
                </button>
                <span className="text-sm text-gray-900">
                  {page + 1} / {totalPages}
                </span>
                <button
                  onClick={() => handlePageChange(page + 1)}
                  disabled={page >= totalPages - 1}
                  className="px-4 py-2 text-sm disabled:opacity-30 disabled:cursor-not-allowed hover:opacity-60 transition"
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
