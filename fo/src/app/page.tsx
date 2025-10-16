'use client';

import React from 'react';
import Link from 'next/link';
import { useCart } from '@/context/CartContext';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { formatCurrency } from '@/lib/formatters';

// 메인 페이지 컴포넌트
export default function Home() {
  const { state: cartState } = useCart();

  // TODO: 실제 데이터 연동
  const featuredProducts = [
    { id: 1, name: 'Sample Product 1', price: 29000, image: '/placeholder.jpg' },
    { id: 2, name: 'Sample Product 2', price: 39000, image: '/placeholder.jpg' },
    { id: 3, name: 'Sample Product 3', price: 49000, image: '/placeholder.jpg' },
  ];

  const recentOrders = [
    { id: 'ORD001', status: '배송중', amount: 58000 },
    { id: 'ORD002', status: '완료', amount: 29000 },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 히어로 섹션 */}
      <section className="bg-gradient-to-r from-blue-600 to-blue-800 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6">
              Vibe Pay
            </h1>
            <p className="text-xl md:text-2xl mb-8 text-blue-100">
              간편하고 안전한 온라인 결제 서비스
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link href="/products">
                <Button size="lg" className="bg-white text-blue-600 hover:bg-gray-100">
                  상품 둘러보기
                </Button>
              </Link>
              <Link href="/order">
                <Button variant="outline" size="lg" className="border-white text-white hover:bg-white hover:text-blue-600">
                  주문하기
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 추천 상품 */}
          <div className="lg:col-span-2">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-gray-900">추천 상품</h2>
              <Link href="/products" className="text-blue-600 hover:text-blue-700 font-medium">
                전체 보기 →
              </Link>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {featuredProducts.map((product) => (
                <Card key={product.id} hoverable className="overflow-hidden">
                  <div className="aspect-square bg-gray-200 mb-4 rounded-t-lg"></div>
                  <Card.Body>
                    <h3 className="font-semibold text-gray-900 mb-2">{product.name}</h3>
                    <p className="text-xl font-bold text-blue-600">
                      {formatCurrency(product.price)}
                    </p>
                  </Card.Body>
                  <Card.Footer>
                    <Link href={`/products/${product.id}`}>
                      <Button fullWidth>상세 보기</Button>
                    </Link>
                  </Card.Footer>
                </Card>
              ))}
            </div>
          </div>

          {/* 사이드바 */}
          <div className="space-y-6">
            {/* 장바구니 요약 */}
            <Card>
              <Card.Header title="장바구니" />
              <Card.Body>
                {cartState.items.length > 0 ? (
                  <div>
                    <p className="text-sm text-gray-600 mb-2">
                      {cartState.totalItems}개 상품
                    </p>
                    <p className="text-lg font-bold text-gray-900 mb-4">
                      {formatCurrency(cartState.totalAmount)}
                    </p>
                    <Link href="/cart">
                      <Button fullWidth>장바구니 보기</Button>
                    </Link>
                  </div>
                ) : (
                  <div className="text-center text-gray-500">
                    <p className="mb-4">장바구니가 비어있습니다</p>
                    <Link href="/products">
                      <Button variant="outline" fullWidth>상품 보러가기</Button>
                    </Link>
                  </div>
                )}
              </Card.Body>
            </Card>

            {/* 최근 주문 */}
            <Card>
              <Card.Header title="최근 주문" />
              <Card.Body>
                {recentOrders.length > 0 ? (
                  <div className="space-y-3">
                    {recentOrders.map((order) => (
                      <div key={order.id} className="flex justify-between items-center py-2 border-b border-gray-100 last:border-b-0">
                        <div>
                          <p className="font-medium text-gray-900">{order.id}</p>
                          <p className="text-sm text-gray-500">{order.status}</p>
                        </div>
                        <p className="font-medium text-gray-900">
                          {formatCurrency(order.amount)}
                        </p>
                      </div>
                    ))}
                    <Link href="/orders">
                      <Button variant="outline" fullWidth size="sm">
                        전체 주문 보기
                      </Button>
                    </Link>
                  </div>
                ) : (
                  <div className="text-center text-gray-500">
                    <p className="mb-4">주문 내역이 없습니다</p>
                    <Link href="/order">
                      <Button variant="outline" fullWidth>주문하기</Button>
                    </Link>
                  </div>
                )}
              </Card.Body>
            </Card>

            {/* 빠른 링크 */}
            <Card>
              <Card.Header title="빠른 링크" />
              <Card.Body>
                <div className="space-y-2">
                  <Link href="/members" className="block w-full">
                    <Button variant="ghost" fullWidth className="justify-start">
                      회원 관리
                    </Button>
                  </Link>
                  <Link href="/products" className="block w-full">
                    <Button variant="ghost" fullWidth className="justify-start">
                      상품 관리
                    </Button>
                  </Link>
                  <Link href="/orders" className="block w-full">
                    <Button variant="ghost" fullWidth className="justify-start">
                      주문 관리
                    </Button>
                  </Link>
                </div>
              </Card.Body>
            </Card>
          </div>
        </div>

        {/* 기능 소개 섹션 */}
        <section className="mt-16">
          <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">
            Vibe Pay의 특별한 기능
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <Card className="text-center">
              <Card.Body>
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">보안 결제</h3>
                <p className="text-gray-600">
                  최고 수준의 보안 기술로 안전한 결제를 보장합니다.
                </p>
              </Card.Body>
            </Card>

            <Card className="text-center">
              <Card.Body>
                <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">빠른 처리</h3>
                <p className="text-gray-600">
                  신속한 결제 처리로 쾌적한 쇼핑 경험을 제공합니다.
                </p>
              </Card.Body>
            </Card>

            <Card className="text-center">
              <Card.Body>
                <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">포인트 적립</h3>
                <p className="text-gray-600">
                  결제할 때마다 포인트가 적립되어 더욱 알뜰하게 쇼핑하세요.
                </p>
              </Card.Body>
            </Card>
          </div>
        </section>
      </div>
    </div>
  );
}
