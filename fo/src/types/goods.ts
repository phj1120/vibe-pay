// 상품 관련 타입 정의

export interface GoodsItem {
  itemNo?: string;
  itemName: string;
  itemPrice: number;
  stock: number;
  goodsStatusCode: string;
  goodsStatusName?: string;
  isSoldOut?: boolean;
}

export interface GoodsRegisterRequest {
  goodsName: string;
  goodsStatusCode: string;
  goodsMainImageUrl: string;
  salePrice: number;
  supplyPrice: number;
  items: GoodsItem[];
}

export interface GoodsModifyRequest {
  goodsName: string;
  goodsStatusCode: string;
  goodsMainImageUrl: string;
  salePrice: number;
  supplyPrice: number;
  items: GoodsItem[];
}

export interface GoodsSearchRequest {
  goodsStatusCode?: string;
  goodsName?: string;
  page?: number;
  size?: number;
}

export interface GoodsListItem {
  goodsNo: string;
  goodsName: string;
  goodsStatusCode: string;
  goodsStatusName: string;
  goodsMainImageUrl: string;
  salePrice: number;
  supplyPrice: number;
  minItemPrice: number;
  maxItemPrice: number;
  totalStock: number;
  isAvailable: boolean;
  registDateTime: string;
}

export interface GoodsPageResponse {
  content: GoodsListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface GoodsDetailResponse {
  goodsNo: string;
  goodsName: string;
  goodsStatusCode: string;
  goodsStatusName: string;
  goodsMainImageUrl: string;
  salePrice: number;
  supplyPrice: number;
  items: GoodsItem[];
  registDateTime: string;
  modifyDateTime: string;
}
