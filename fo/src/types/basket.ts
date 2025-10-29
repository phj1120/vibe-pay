// 장바구니 관련 타입 정의

export interface BasketItem {
  basketNo: string;
  memberNo: string;
  goodsNo: string;
  goodsName: string;
  goodsStatusCode: string;
  goodsMainImageUrl: string;
  itemNo: string;
  itemName: string;
  itemPrice: number;
  itemStatusCode: string;
  stock: number;
  quantity: number;
  isOrder: boolean;
  registDateTime: string;
}

export interface BasketAddRequest {
  goodsNo: string;
  itemNo: string;
  quantity: number;
}

export interface BasketModifyRequest {
  goodsNo?: string;
  itemNo?: string;
  quantity?: number;
}
