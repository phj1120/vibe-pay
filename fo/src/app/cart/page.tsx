'use client';

import React from 'react';
import Link from 'next/link';
import { useCart } from '@/context/CartContext';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { formatCurrency } from '@/lib/formatters';

/**
 * 장바구니 페이지 컴포넌트
 *
 * 주요 기능:
 * - 장바구니 아이템 목록 표시
 * - 수량 증가/감소
 * - 개별 아이템 삭제
 * - 장바구니 전체 비우기
 * - 총 금액 계산 및 표시
 * - 주문 페이지로 이동
 */
export default function CartPage() {
  const { state, updateQuantity, removeItem, clearCart } = useCart();
  const { items, totalItems, totalAmount } = state;

  // 빈 장바구니 UI
  if (items.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <Card className="max-w-2xl mx-auto">
          <Card.Body>
            <div className="flex flex-col items-center justify-center py-12">
              <svg
                className="w-24 h-24 text-gray-300 mb-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
                />
              </svg>
              <h2 className="text-2xl font-bold text-gray-700 mb-2">장바구니가 비어있습니다</h2>
              <p className="text-gray-500 mb-6">상품을 담아주세요</p>
              <Link href="/products">
                <Button>상품 보러가기</Button>
              </Link>
            </div>
          </Card.Body>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        {/* 헤더 */}
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold text-gray-900">장바구니</h1>
          <Button
            variant="outline"
            onClick={clearCart}
            className="text-red-600 hover:text-red-700 hover:bg-red-50"
          >
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
            </svg>
            전체 비우기
          </Button>
        </div>

        {/* 장바구니 아이템 목록 */}
        <div className="space-y-4 mb-6">
          {items.map((item) => (
            <Card key={item.product.productId}>
              <Card.Body>
                <div className="flex gap-4">
                  {/* 상품 이미지 플레이스홀더 */}
                  <div className="w-24 h-24 flex-shrink-0 bg-gray-200 rounded-lg flex items-center justify-center">
                    <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>

                  {/* 상품 정보 */}
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-lg text-gray-900 mb-1 truncate">
                      {item.product.name}
                    </h3>
                    <p className="text-lg font-bold text-blue-600 mb-3">
                      {formatCurrency(item.product.price)}
                    </p>

                    {/* 수량 조절 */}
                    <div className="flex items-center gap-3">
                      <div className="flex items-center gap-2 bg-gray-100 rounded-lg p-1">
                        <button
                          onClick={() => updateQuantity(item.product.productId, item.quantity - 1)}
                          className="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-200 transition-colors"
                          aria-label="수량 감소"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                          </svg>
                        </button>
                        <span className="w-12 text-center font-semibold">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => updateQuantity(item.product.productId, item.quantity + 1)}
                          className="w-8 h-8 flex items-center justify-center rounded hover:bg-gray-200 transition-colors"
                          aria-label="수량 증가"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                          </svg>
                        </button>
                      </div>

                      {/* 삭제 버튼 */}
                      <button
                        onClick={() => removeItem(item.product.productId)}
                        className="text-red-600 hover:text-red-700 p-2 rounded hover:bg-red-50 transition-colors"
                        aria-label="상품 삭제"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>

                  {/* 소계 */}
                  <div className="text-right flex-shrink-0">
                    <p className="text-sm text-gray-500 mb-1">소계</p>
                    <p className="text-xl font-bold text-gray-900">
                      {formatCurrency(item.product.price * item.quantity)}
                    </p>
                  </div>
                </div>
              </Card.Body>
            </Card>
          ))}
        </div>

        {/* 주문 요약 */}
        <Card className="sticky bottom-4">
          <Card.Header title="주문 요약" />
          <Card.Body>
            <div className="space-y-3">
              <div className="flex justify-between text-gray-600">
                <span>상품 수량</span>
                <span className="font-semibold">{totalItems}개</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>상품 금액</span>
                <span className="font-semibold">{formatCurrency(totalAmount)}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>배송비</span>
                <span className="font-semibold text-green-600">무료</span>
              </div>
              <div className="border-t pt-3 mt-3">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold text-gray-900">총 결제금액</span>
                  <span className="text-2xl font-bold text-blue-600">
                    {formatCurrency(totalAmount)}
                  </span>
                </div>
              </div>
            </div>
          </Card.Body>
          <Card.Footer>
            <div className="flex gap-3">
              <Link href="/products" className="flex-1">
                <Button variant="outline" fullWidth>
                  쇼핑 계속하기
                </Button>
              </Link>
              <Link href="/order" className="flex-1">
                <Button fullWidth className="bg-blue-600 hover:bg-blue-700">
                  주문하기
                </Button>
              </Link>
            </div>
          </Card.Footer>
        </Card>
      </div>
    </div>
  );
}
