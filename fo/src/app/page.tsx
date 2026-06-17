"use client";

import { Suspense, useState, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Image from "next/image";
import { getGoodsList, getGoodsDetail } from "@/lib/goods-api";
import type { GoodsListItem, GoodsSearchRequest, GoodsItem } from "@/types/goods";
import { useBasketStore } from "@/store/basket-store";
import { useToast } from "@/hooks/useToast";
import Toast from "@/components/ui/Toast";
import LoginRequiredModal from "@/components/ui/LoginRequiredModal";
import { useLoginRequired } from "@/hooks/useLoginRequired";
import ItemSelectionModal from "@/components/common/ItemSelectionModal";

function HomeContent() {
  const router = useRouter();
  const urlSearchParams = useSearchParams();
  const [goods, setGoods] = useState<GoodsListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [searchParams, setSearchParams] = useState<GoodsSearchRequest>({
    page: 0,
    size: 20,
  });
  const [addingToBasket, setAddingToBasket] = useState<string | null>(null);
  const [isItemModalOpen, setIsItemModalOpen] = useState(false);
  const [selectedGoods, setSelectedGoods] = useState<{
    goodsNo: string;
    goodsName: string;
    items: GoodsItem[];
  } | null>(null);

  const { addBasket } = useBasketStore();
  const { toasts, removeToast, success, error: showError } = useToast();
  const { isModalOpen, checkLoginRequired, closeModal } = useLoginRequired();

  // URL 쿼리스트링에서 검색어 읽기
  useEffect(() => {
    const keyword = urlSearchParams.get("keyword") || "";
    setSearchKeyword(keyword);
    setSearchParams({
      page: 0,
      size: 20,
      goodsName: keyword || undefined,
    });
  }, [urlSearchParams]);

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
    // URL 쿼리스트링 업데이트
    const params = new URLSearchParams();
    if (goodsName) {
      params.set("keyword", goodsName);
    }
    router.push(`/?${params.toString()}`);
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

  function isGoodsAvailable(item: GoodsListItem): boolean {
    return item.goodsStatusCode === "001";
  }

  async function handleAddToBasket(
    event: React.MouseEvent,
    goodsNo: string,
    goodsName: string,
    isAvailable: boolean
  ) {
    event.stopPropagation();

    // 로그인 체크
    if (!checkLoginRequired()) {
      return;
    }

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
        setAddingToBasket(null);
        return;
      }

      // 재고가 있는 단품 필터링
      const availableItems = goodsDetail.items.filter(
        (item) => item.stock > 0 && !item.isSoldOut
      );

      if (availableItems.length === 0) {
        showError("재고가 없습니다");
        setAddingToBasket(null);
        return;
      }

      // 단품 선택 모달 열기
      setSelectedGoods({
        goodsNo,
        goodsName,
        items: goodsDetail.items,
      });
      setIsItemModalOpen(true);
      setAddingToBasket(null);
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "상품 정보를 불러올 수 없습니다";
      showError(errorMessage);
      setAddingToBasket(null);
    }
  }

  async function handleItemSelect(items: Array<{ itemNo: string; quantity: number }>) {
    if (!selectedGoods) return;

    try {
      // 여러 단품을 순차적으로 장바구니에 추가
      for (const item of items) {
        await addBasket({
          goodsNo: selectedGoods.goodsNo,
          itemNo: item.itemNo,
          quantity: item.quantity,
        });
      }

      const itemCount = items.length;
      success(
        `${selectedGoods.goodsName} ${itemCount}개 단품을 장바구니에 담았습니다`
      );
    } catch (err) {
      const errorMessage =
        err instanceof Error ? err.message : "장바구니 담기에 실패했습니다";
      showError(errorMessage);
    }
  }

  function handleItemModalClose() {
    setIsItemModalOpen(false);
    setSelectedGoods(null);
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
      <LoginRequiredModal isOpen={isModalOpen} onClose={closeModal} />
      <ItemSelectionModal
        isOpen={isItemModalOpen}
        onClose={handleItemModalClose}
        onSelect={handleItemSelect}
        items={selectedGoods?.items || []}
        goodsName={selectedGoods?.goodsName || ""}
      />
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
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              placeholder="상품 검색"
              className="flex-1 px-4 py-3 border border-gray-300 focus:outline-none focus:border-black transition"
            />
            <button
              type="submit"
              className="px-8 py-3 bg-black text-white hover:bg-gray-800 transition"
            >
              검색
            </button>
            <button
              type="button"
              onClick={() => router.push("/goods/new")}
              className="px-4 py-3 bg-black text-white hover:bg-gray-800 transition flex items-center justify-center"
              title="상품 등록"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 4v16m8-8H4"
                />
              </svg>
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
              {goods.map((item) => {
                const isAvailable = isGoodsAvailable(item);

                return (
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
                      {!isAvailable && (
                        <div className="absolute inset-0 bg-black bg-opacity-60 flex items-center justify-center">
                          <span className="text-white text-sm font-medium">SOLD OUT</span>
                        </div>
                      )}
                    </div>
                    <div>
                      <h3 className="font-medium text-base mb-2 text-gray-900">{item.goodsName}</h3>
                      <div className="text-sm text-gray-900 font-medium">
                        {formatPrice(item.salePrice)}
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={(e) =>
                      handleAddToBasket(e, item.goodsNo, item.goodsName, isAvailable)
                    }
                    disabled={!isAvailable || addingToBasket === item.goodsNo}
                    className="w-full mt-3 py-2.5 bg-black text-white text-sm hover:bg-gray-800 disabled:bg-gray-300 disabled:cursor-not-allowed transition"
                  >
                    {addingToBasket === item.goodsNo
                      ? "담는 중..."
                      : "장바구니"}
                  </button>
                </div>
                );
              })}
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

export default function Home() {
  return (
    <Suspense
      fallback={
        <div className="flex justify-center items-center min-h-screen">
          <div className="text-sm text-gray-400">로딩 중...</div>
        </div>
      }
    >
      <HomeContent />
    </Suspense>
  );
}
