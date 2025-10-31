import { BasketItem } from "./basket";

export interface OrderSheet {
  memberNo: string;
  items: BasketItem[];
  ordererName: string;
  ordererEmail: string;
  ordererPhone: string;
  totalProductAmount: number;
  totalQuantity: number;
}
