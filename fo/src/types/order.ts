import { BasketItem } from "./basket";

export interface OrderSheet {
  items: BasketItem[];
  ordererName: string;
  ordererEmail: string;
  ordererPhone: string;
  totalProductAmount: number;
  totalQuantity: number;
}

// 주문 목록 응답
export interface OrderListResponse {
  orderNo: string;
  orderAcceptDtm: string;
  totalAmount: number;
  goodsList: OrderListGoods[];
}

export interface OrderListGoods {
  orderSequence: number;
  orderProcessSequence: number;
  goodsNo: string;
  itemNo: string;
  goodsName: string;
  itemName: string;
  salePrice: number;
  quantity: number;
  orderStatusCode: string;
  orderStatusName: string;
  orderTypeCode: string;
  orderTypeName: string;
  cancelable: boolean;
  cancelableAmount: number;
}

// 주문 취소 요청
export interface CancelRequest {
  targets: ClaimTargetRequest[];
}

export interface ClaimTargetRequest {
  orderNo: string;
  orderSequence: number;
  orderProcessSequence: number;
}

// 취소 가능한 주문 조회 응답
export interface CancelableOrderResponse {
  orderNo: string;
  cancelableItems: CancelableOrderItem[];
  refundInfo: RefundInfo;
}

export interface CancelableOrderItem {
  orderSequence: number;
  orderProcessSequence: number;
  goodsNo: string;
  itemNo: string;
  goodsName: string;
  itemName: string;
  salePrice: number;
  quantity: number;
  subtotal: number;
}

export interface RefundInfo {
  totalRefundAmount: number;
  refundDetails: RefundDetail[];
}

export interface RefundDetail {
  payWayCode: string;
  payWayName: string;
  refundAmount: number;
  pgTypeCode?: string;
  pgTypeName?: string;
}
