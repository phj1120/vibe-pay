'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'

interface Product {
  productId: number
  name: string
  price: number
}

interface ProductFormData {
  name: string
  price: number
}

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const [showDialog, setShowDialog] = useState(false)
  const [showDeleteDialog, setShowDeleteDialog] = useState(false)
  const [isEditMode, setIsEditMode] = useState(false)
  const [editedProduct, setEditedProduct] = useState<ProductFormData>({
    name: '',
    price: 0
  })
  const [currentEditId, setCurrentEditId] = useState<number | null>(null)
  const [productToDelete, setProductToDelete] = useState<Product | null>(null)

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const response = await fetch('http://localhost:8080/api/products')
      if (response.ok) {
        const data = await response.json()
        setProducts(data)
      } else {
        alert('상품 목록 조회 실패')
      }
    } catch (error) {
      console.error(error)
      alert('상품 목록 조회 중 오류 발생')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts()
  }, [])

  const openCreateDialog = () => {
    setIsEditMode(false)
    setEditedProduct({
      name: '',
      price: 0
    })
    setCurrentEditId(null)
    setShowDialog(true)
  }

  const openEditDialog = (product: Product) => {
    setIsEditMode(true)
    setEditedProduct({
      name: product.name,
      price: product.price
    })
    setCurrentEditId(product.productId)
    setShowDialog(true)
  }

  const saveProduct = async () => {
    if (!editedProduct.name || editedProduct.price <= 0) {
      alert('상품명과 가격을 올바르게 입력해주세요.')
      return
    }

    const url = isEditMode
      ? `http://localhost:8080/api/products/${currentEditId}`
      : 'http://localhost:8080/api/products'

    const method = isEditMode ? 'PUT' : 'POST'

    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editedProduct)
      })

      if (response.ok) {
        setShowDialog(false)
        fetchProducts()
      } else {
        alert('저장 실패')
      }
    } catch (error) {
      console.error(error)
      alert('저장 중 오류 발생')
    }
  }

  const openDeleteDialog = (product: Product) => {
    setProductToDelete(product)
    setShowDeleteDialog(true)
  }

  const deleteProduct = async () => {
    if (!productToDelete) return

    try {
      const response = await fetch(
        `http://localhost:8080/api/products/${productToDelete.productId}`,
        { method: 'DELETE' }
      )

      if (response.ok) {
        setShowDeleteDialog(false)
        setProductToDelete(null)
        fetchProducts()
      } else if (response.status === 409) {
        alert('주문 내역이 있는 상품은 삭제할 수 없습니다.')
      } else {
        alert('삭제 실패')
      }
    } catch (error) {
      console.error(error)
      alert('삭제 중 오류 발생')
    }
  }

  const formatPrice = (price: number) => {
    return `₩${price.toLocaleString()}`
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      {/* Header */}
      <div className="max-w-7xl mx-auto mb-8">
        <div className="flex items-center justify-between">
          <div>
            <Link
              href="/"
              className="text-gray-600 hover:text-gray-900 mb-2 inline-block"
            >
              ← 뒤로가기
            </Link>
            <h1 className="text-3xl font-bold text-gray-900">상품 관리</h1>
            <p className="text-gray-600 mt-2">상품 목록 및 정보 관리</p>
          </div>
          <button
            onClick={openCreateDialog}
            className="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-lg font-semibold shadow-md transition-colors flex items-center gap-2"
          >
            <span className="text-xl">+</span>
            상품 추가
          </button>
        </div>
      </div>

      {/* Products Grid */}
      <div className="max-w-7xl mx-auto">
        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
            <p className="mt-2 text-gray-600">로딩 중...</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {products.map((product) => (
              <div
                key={product.productId}
                className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow overflow-hidden"
              >
                {/* Product Image Placeholder */}
                <div className="h-48 bg-gray-200 flex items-center justify-center">
                  <svg
                    className="w-24 h-24 text-gray-400"
                    fill="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path d="M20 6h-2.18c.11-.31.18-.65.18-1a2.996 2.996 0 0 0-5.5-1.65l-.5.67-.5-.68C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zM9 4c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm11 15H4v-2h16v2zm0-5H4V8h5.08L7 10.83 8.62 12 12 7.4l3.38 4.6L17 10.83 14.92 8H20v6z" />
                  </svg>
                </div>

                {/* Product Info */}
                <div className="p-4">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2 truncate">
                    {product.name}
                  </h3>
                  <p className="text-2xl font-bold text-green-600 mb-4">
                    {formatPrice(product.price)}
                  </p>

                  {/* Actions */}
                  <div className="flex gap-2">
                    <button
                      onClick={() => openEditDialog(product)}
                      className="flex-1 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded transition-colors"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => openDeleteDialog(product)}
                      className="flex-1 bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded transition-colors"
                    >
                      삭제
                    </button>
                    <Link
                      href={`/products/${product.productId}`}
                      className="flex-1 bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded transition-colors text-center"
                    >
                      상세
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {!loading && products.length === 0 && (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <p className="text-gray-600">등록된 상품이 없습니다.</p>
            <button
              onClick={openCreateDialog}
              className="mt-4 text-green-600 hover:text-green-700 font-semibold"
            >
              첫 상품을 추가해보세요
            </button>
          </div>
        )}
      </div>

      {/* Create/Edit Dialog */}
      {showDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold mb-4">
              {isEditMode ? '상품 수정' : '상품 추가'}
            </h2>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  상품명 *
                </label>
                <input
                  type="text"
                  value={editedProduct.name}
                  onChange={(e) =>
                    setEditedProduct({ ...editedProduct, name: e.target.value })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="상품명을 입력하세요"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  가격 *
                </label>
                <input
                  type="number"
                  value={editedProduct.price}
                  onChange={(e) =>
                    setEditedProduct({
                      ...editedProduct,
                      price: Number(e.target.value)
                    })
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="가격을 입력하세요"
                  min="0"
                />
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowDialog(false)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              >
                취소
              </button>
              <button
                onClick={saveProduct}
                className="flex-1 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
              >
                {isEditMode ? '수정' : '생성'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Dialog */}
      {showDeleteDialog && productToDelete && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold mb-4 text-red-600">상품 삭제</h2>
            <p className="text-gray-700 mb-6">
              정말로 <strong>{productToDelete.name}</strong>을(를)
              삭제하시겠습니까?
              <br />이 작업은 되돌릴 수 없습니다.
            </p>

            <div className="flex gap-3">
              <button
                onClick={() => {
                  setShowDeleteDialog(false)
                  setProductToDelete(null)
                }}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
              >
                취소
              </button>
              <button
                onClick={deleteProduct}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
