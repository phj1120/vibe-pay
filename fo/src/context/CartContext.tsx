'use client';

import React, { createContext, useContext, useReducer, ReactNode } from 'react';
import { Product } from '@/types/product';

// 장바구니 아이템 타입
export interface CartItem {
  product: Product;
  quantity: number;
}

// 장바구니 상태 타입
export interface CartState {
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
}

// 장바구니 액션 타입
export type CartAction =
  | { type: 'ADD_ITEM'; payload: { product: Product; quantity: number } }
  | { type: 'REMOVE_ITEM'; payload: { productId: number } }
  | { type: 'UPDATE_QUANTITY'; payload: { productId: number; quantity: number } }
  | { type: 'CLEAR_CART' }
  | { type: 'LOAD_CART'; payload: CartState };

// 초기 상태
const initialState: CartState = {
  items: [],
  totalItems: 0,
  totalAmount: 0,
};

// 상태 계산 함수
const calculateTotals = (items: CartItem[]): { totalItems: number; totalAmount: number } => {
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalAmount = items.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  return { totalItems, totalAmount };
};

// 리듀서 함수
const cartReducer = (state: CartState, action: CartAction): CartState => {
  switch (action.type) {
    case 'ADD_ITEM': {
      const { product, quantity } = action.payload;
      const existingItemIndex = state.items.findIndex(item => item.product.productId === product.productId);

      let newItems: CartItem[];

      if (existingItemIndex >= 0) {
        // 기존 아이템이 있으면 수량 증가
        newItems = state.items.map((item, index) =>
          index === existingItemIndex
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        // 새 아이템 추가
        newItems = [...state.items, { product, quantity }];
      }

      const { totalItems, totalAmount } = calculateTotals(newItems);

      return {
        ...state,
        items: newItems,
        totalItems,
        totalAmount,
      };
    }

    case 'REMOVE_ITEM': {
      const newItems = state.items.filter(item => item.product.productId !== action.payload.productId);
      const { totalItems, totalAmount } = calculateTotals(newItems);

      return {
        ...state,
        items: newItems,
        totalItems,
        totalAmount,
      };
    }

    case 'UPDATE_QUANTITY': {
      const { productId, quantity } = action.payload;

      if (quantity <= 0) {
        // 수량이 0 이하이면 아이템 제거
        return cartReducer(state, { type: 'REMOVE_ITEM', payload: { productId } });
      }

      const newItems = state.items.map(item =>
        item.product.productId === productId
          ? { ...item, quantity }
          : item
      );

      const { totalItems, totalAmount } = calculateTotals(newItems);

      return {
        ...state,
        items: newItems,
        totalItems,
        totalAmount,
      };
    }

    case 'CLEAR_CART':
      return initialState;

    case 'LOAD_CART':
      return action.payload;

    default:
      return state;
  }
};

// 컨텍스트 타입
interface CartContextType {
  state: CartState;
  addItem: (product: Product, quantity?: number) => void;
  removeItem: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  getItemQuantity: (productId: number) => number;
  isInCart: (productId: number) => boolean;
}

// 컨텍스트 생성
const CartContext = createContext<CartContextType | undefined>(undefined);

// 프로바이더 컴포넌트
export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(cartReducer, initialState);

  // 로컬스토리지에서 장바구니 상태 로드
  React.useEffect(() => {
    try {
      const savedCart = localStorage.getItem('cart');
      if (savedCart) {
        const cartData = JSON.parse(savedCart) as CartState;
        dispatch({ type: 'LOAD_CART', payload: cartData });
      }
    } catch (error) {
      console.error('Failed to load cart from localStorage:', error);
    }
  }, []);

  // 장바구니 상태 변경 시 로컬스토리지에 저장
  React.useEffect(() => {
    try {
      localStorage.setItem('cart', JSON.stringify(state));
    } catch (error) {
      console.error('Failed to save cart to localStorage:', error);
    }
  }, [state]);

  const addItem = (product: Product, quantity: number = 1) => {
    dispatch({ type: 'ADD_ITEM', payload: { product, quantity } });
  };

  const removeItem = (productId: number) => {
    dispatch({ type: 'REMOVE_ITEM', payload: { productId } });
  };

  const updateQuantity = (productId: number, quantity: number) => {
    dispatch({ type: 'UPDATE_QUANTITY', payload: { productId, quantity } });
  };

  const clearCart = () => {
    dispatch({ type: 'CLEAR_CART' });
  };

  const getItemQuantity = (productId: number): number => {
    const item = state.items.find(item => item.product.productId === productId);
    return item ? item.quantity : 0;
  };

  const isInCart = (productId: number): boolean => {
    return state.items.some(item => item.product.productId === productId);
  };

  const value: CartContextType = {
    state,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    getItemQuantity,
    isInCart,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

// 훅
export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};