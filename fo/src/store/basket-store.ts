import { create } from "zustand";
import type { BasketItem, BasketAddRequest, BasketModifyRequest } from "@/types/basket";
import * as basketApi from "@/lib/basket-api";

interface BasketStore {
  baskets: BasketItem[];
  selectedBasketNos: string[];
  isLoading: boolean;
  error: string | null;

  // Actions
  fetchBaskets: () => Promise<void>;
  addBasket: (data: BasketAddRequest) => Promise<string>;
  modifyBasket: (basketNo: string, data: BasketModifyRequest) => Promise<void>;
  deleteBasket: (basketNo: string) => Promise<void>;
  deleteSelectedBaskets: () => Promise<void>;
  deleteAllBaskets: () => Promise<void>;
  toggleSelectBasket: (basketNo: string) => void;
  toggleSelectAll: () => void;
  clearSelection: () => void;
}

export const useBasketStore = create<BasketStore>((set, get) => ({
  baskets: [],
  selectedBasketNos: [],
  isLoading: false,
  error: null,

  fetchBaskets: async () => {
    try {
      set({ isLoading: true, error: null });
      const baskets = await basketApi.getBasketList();
      set({ baskets, isLoading: false });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : "장바구니 조회에 실패했습니다",
        isLoading: false,
      });
      throw error;
    }
  },

  addBasket: async (data: BasketAddRequest) => {
    try {
      set({ isLoading: true, error: null });
      const basketNo = await basketApi.addBasket(data);
      await get().fetchBaskets();
      set({ isLoading: false });
      return basketNo;
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : "장바구니 추가에 실패했습니다",
        isLoading: false,
      });
      throw error;
    }
  },

  modifyBasket: async (basketNo: string, data: BasketModifyRequest) => {
    try {
      set({ isLoading: true, error: null });
      await basketApi.modifyBasket(basketNo, data);
      await get().fetchBaskets();
      set({ isLoading: false });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : "장바구니 수정에 실패했습니다",
        isLoading: false,
      });
      throw error;
    }
  },

  deleteBasket: async (basketNo: string) => {
    try {
      set({ isLoading: true, error: null });
      await basketApi.deleteBasket(basketNo);
      await get().fetchBaskets();
      set({ isLoading: false });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : "장바구니 삭제에 실패했습니다",
        isLoading: false,
      });
      throw error;
    }
  },

  deleteSelectedBaskets: async () => {
    const { selectedBasketNos } = get();
    if (selectedBasketNos.length === 0) return;

    try {
      set({ isLoading: true, error: null });
      await basketApi.deleteBaskets(selectedBasketNos);
      await get().fetchBaskets();
      set({ isLoading: false, selectedBasketNos: [] });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : "선택한 장바구니 삭제에 실패했습니다",
        isLoading: false,
      });
      throw error;
    }
  },

  deleteAllBaskets: async () => {
    try {
      set({ isLoading: true, error: null });
      await basketApi.deleteAllBaskets();
      await get().fetchBaskets();
      set({ isLoading: false, selectedBasketNos: [] });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : "장바구니 전체 삭제에 실패했습니다",
        isLoading: false,
      });
      throw error;
    }
  },

  toggleSelectBasket: (basketNo: string) => {
    const { selectedBasketNos } = get();
    const isSelected = selectedBasketNos.includes(basketNo);

    if (isSelected) {
      set({ selectedBasketNos: selectedBasketNos.filter((no) => no !== basketNo) });
    } else {
      set({ selectedBasketNos: [...selectedBasketNos, basketNo] });
    }
  },

  toggleSelectAll: () => {
    const { baskets, selectedBasketNos } = get();
    if (selectedBasketNos.length === baskets.length) {
      set({ selectedBasketNos: [] });
    } else {
      set({ selectedBasketNos: baskets.map((basket) => basket.basketNo) });
    }
  },

  clearSelection: () => {
    set({ selectedBasketNos: [] });
  },
}));
