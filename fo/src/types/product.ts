// 상품 타입
export interface Product {
  productId: number;
  name: string;
  price: number;
}

// 상품 생성 요청 타입
export interface CreateProductRequest {
  name: string;
  price: number;
}

// 상품 수정 요청 타입
export interface UpdateProductRequest {
  name?: string;
  price?: number;
}

// 상품 검색 조건 타입
export interface ProductSearchCondition {
  name?: string;
  minPrice?: number;
  maxPrice?: number;
}