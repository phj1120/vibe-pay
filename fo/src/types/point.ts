export interface PointTransactionRequest {
  amount: number;
  pointTransactionCode: string;
  pointTransactionReasonCode: string;
  pointTransactionReasonNo?: string;
}

export interface PointBalanceResponse {
  totalPoint: number;
}

export interface PointHistoryResponse {
  pointHistoryNo: string;
  amount: number;
  pointTransactionCode: string;
  pointTransactionReasonCode: string;
  pointTransactionReasonNo?: string;
  startDateTime?: string;
  endDateTime?: string;
  remainPoint?: number;
  createdDate: string;
}

export interface PointHistoryListResponse {
  content: PointHistoryResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
