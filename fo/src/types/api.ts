// API 공통 응답 타입
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  timestamp: string;
}

// API 에러 응답 타입
export interface ApiError {
  success: false;
  message: string;
  errorCode: string;
  timestamp: string;
}

// 페이지네이션 요청 타입
export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

// 페이지네이션 응답 타입
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}