'use client';

import React from 'react';
import Link from 'next/link';
import { Product } from '@/types/product';
import { PageResponse } from '@/types/api';
import { getProducts } from '@/lib/api/products';
import { useCart } from '@/context/CartContext';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Loading from '@/components/ui/Loading';
import { formatCurrency } from '@/lib/formatters';

// 상품 목록 페이지 컴포넌트
export default function ProductsPage() {
  const { addItem, getItemQuantity } = useCart();
  const [products, setProducts] = React.useState<PageResponse<Product>>();
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string>();
  const [searchKeyword, setSearchKeyword] = React.useState('');

  // 상품 목록 조회
  const fetchProducts = React.useCallback(async (keyword?: string) => {
    try {
      setLoading(true);
      setError(undefined);
      const params = keyword ? { name: keyword } : undefined;
      const data = await getProducts(params);
      setProducts(data);
    } catch (err) {
      setError('상품 목록을 불러오는데 실패했습니다.');
      console.error('Failed to fetch products:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  // 검색 처리
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchProducts(searchKeyword.trim());
  };

  // 장바구니 추가
  const handleAddToCart = (product: Product) => {
    addItem(product, 1);
    alert(`${product.name}이(가) 장바구니에 추가되었습니다.`);
  };

  // 컴포넌트 마운트 시 상품 목록 조회
  React.useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  return (
    <div className="container mx-auto px-4 py-8">
      {/* 헤더 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">상품 목록</h1>
        <p className="text-gray-600">다양한 상품을 둘러보고 주문해보세요.</p>
      </div>

      {/* 검색 */}
      <div className="mb-6">
        <form onSubmit={handleSearch} className="flex gap-2 max-w-md">
          <Input
            type="text"
            placeholder="상품명으로 검색..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="flex-1"
          />
          <Button type="submit" disabled={loading}>
            검색
          </Button>
        </form>
      </div>

      {/* 에러 상태 */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p className="text-red-800">{error}</p>
        </div>
      )}

      {/* 로딩 상태 */}
      {loading && !products && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="animate-pulse">
              <div className="bg-gray-200 aspect-square rounded-lg mb-4"></div>
              <div className="h-4 bg-gray-200 rounded mb-2"></div>
              <div className="h-6 bg-gray-200 rounded mb-4"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
      )}

      {/* 상품 목록 */}
      {products && (
        <>
          {/* 상품 개수 */}
          <div className="mb-6">
            <p className="text-gray-600">
              총 {products.totalElements}개의 상품
              {searchKeyword && ` (검색: "${searchKeyword}")`}
            </p>
          </div>

          {/* 상품 그리드 */}
          {products.content.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {products.content.map((product) => {
                const cartQuantity = getItemQuantity(product.productId);

                return (
                  <Card key={product.productId} hoverable className="overflow-hidden">
                    {/* 상품 이미지 */}
                    <div className="aspect-square bg-gray-200 relative">
                      {/* TODO: 실제 상품 이미지 */}
                      <div className="absolute inset-0 flex items-center justify-center text-gray-400">
                        <svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                      {cartQuantity > 0 && (
                        <div className="absolute top-2 right-2 bg-blue-600 text-white text-xs px-2 py-1 rounded-full">
                          장바구니 {cartQuantity}개
                        </div>
                      )}
                    </div>

                    <Card.Body>
                      <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2">
                        {product.name}
                      </h3>
                      <p className="text-2xl font-bold text-blue-600 mb-4">
                        {formatCurrency(product.price)}
                      </p>
                    </Card.Body>

                    <Card.Footer>
                      <div className="flex gap-2">
                        <Link href={`/products/${product.productId}`} className="flex-1">
                          <Button variant="outline" fullWidth size="sm">
                            상세보기
                          </Button>
                        </Link>
                        <Button
                          onClick={() => handleAddToCart(product)}
                          className="flex-1"
                          size="sm"
                        >
                          장바구니
                        </Button>
                      </div>
                    </Card.Footer>
                  </Card>
                );
              })}
            </div>
          ) : (
            /* 빈 상태 */
            <div className="text-center py-12">
              <svg
                className="mx-auto h-12 w-12 text-gray-400 mb-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
                />
              </svg>
              <h3 className="text-lg font-medium text-gray-900 mb-2">상품이 없습니다</h3>
              <p className="text-gray-500">
                {searchKeyword ? '검색 조건에 맞는 상품이 없습니다.' : '등록된 상품이 없습니다.'}
              </p>
            </div>
          )}

          {/* 페이지네이션 */}
          {products.content.length > 0 && (
            <div className="mt-8 flex justify-center">
              <div className="flex items-center space-x-2">
                <Button
                  variant="outline"
                  disabled={products.first}
                  onClick={() => {
                    // TODO: 이전 페이지 로직
                  }}
                >
                  이전
                </Button>
                <span className="text-sm text-gray-600">
                  {products.number + 1} / {products.totalPages}
                </span>
                <Button
                  variant="outline"
                  disabled={products.last}
                  onClick={() => {
                    // TODO: 다음 페이지 로직
                  }}
                >
                  다음
                </Button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}