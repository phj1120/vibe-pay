'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'

interface Product {
  productId: number
  name: string
  price: number
}

export default function ProductDetailPage({
  params
}: {
  params: { id: string }
}) {
  const router = useRouter()
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)

  const fetchProduct = async () => {
    setLoading(true)
    try {
      const response = await fetch(
        `http://localhost:8080/api/products/${params.id}`
      )
      if (response.ok) {
        const data = await response.json()
        setProduct(data)
      } else if (response.status === 404) {
        alert('상품을 찾을 수 없습니다.')
        router.push('/products')
      } else {
        alert('상품 정보 조회 실패')
      }
    } catch (error) {
      console.error(error)
      alert('상품 정보 조회 중 오류 발생')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProduct()
  }, [params.id])

  const formatPrice = (price: number) => {
    return `₩${price.toLocaleString()}`
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          <p className="mt-4 text-gray-600">로딩 중...</p>
        </div>
      </div>
    )
  }

  if (!product) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">상품을 찾을 수 없습니다.</p>
          <Link
            href="/products"
            className="mt-4 inline-block text-green-600 hover:text-green-700 font-semibold"
          >
            상품 목록으로 돌아가기
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      {/* Header */}
      <div className="max-w-4xl mx-auto mb-8">
        <Link
          href="/products"
          className="text-gray-600 hover:text-gray-900 mb-4 inline-block"
        >
          ← 상품 목록으로
        </Link>
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>
          <Link
            href="/products"
            className="bg-gray-500 hover:bg-gray-600 text-white px-6 py-2 rounded-lg transition-colors"
          >
            목록으로
          </Link>
        </div>
      </div>

      {/* Product Detail Card */}
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow-lg overflow-hidden">
          {/* Product Image Section */}
          <div className="grid md:grid-cols-2 gap-8 p-8">
            {/* Image Placeholder */}
            <div className="aspect-square bg-gray-200 rounded-lg flex items-center justify-center">
              <svg
                className="w-48 h-48 text-gray-400"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                <path d="M20 6h-2.18c.11-.31.18-.65.18-1a2.996 2.996 0 0 0-5.5-1.65l-.5.67-.5-.68C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zM9 4c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm11 15H4v-2h16v2zm0-5H4V8h5.08L7 10.83 8.62 12 12 7.4l3.38 4.6L17 10.83 14.92 8H20v6z" />
              </svg>
            </div>

            {/* Product Info */}
            <div className="flex flex-col justify-center">
              <div className="mb-6">
                <h2 className="text-sm font-medium text-gray-500 mb-1">
                  상품 ID
                </h2>
                <p className="text-lg text-gray-900">#{product.productId}</p>
              </div>

              <div className="mb-6">
                <h2 className="text-sm font-medium text-gray-500 mb-1">
                  상품명
                </h2>
                <p className="text-2xl font-bold text-gray-900">
                  {product.name}
                </p>
              </div>

              <div className="mb-6">
                <h2 className="text-sm font-medium text-gray-500 mb-1">가격</h2>
                <p className="text-3xl font-bold text-green-600">
                  {formatPrice(product.price)}
                </p>
              </div>

              <div className="flex gap-3 mt-8">
                <Link
                  href="/products"
                  className="flex-1 bg-gray-500 hover:bg-gray-600 text-white px-6 py-3 rounded-lg font-semibold text-center transition-colors"
                >
                  목록으로
                </Link>
                <Link
                  href={`/products?edit=${product.productId}`}
                  className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-semibold text-center transition-colors"
                >
                  수정하기
                </Link>
              </div>
            </div>
          </div>

          {/* Additional Info Section */}
          <div className="border-t border-gray-200 p-8">
            <h2 className="text-xl font-bold text-gray-900 mb-4">
              주문 내역 (준비 중)
            </h2>
            <div className="bg-gray-50 rounded-lg p-6 text-center">
              <p className="text-gray-600">
                이 상품이 포함된 주문 내역은 Phase 2-5에서 구현 예정입니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
