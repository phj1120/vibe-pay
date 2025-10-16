// 회원 타입
export interface Member {
  memberId: number;
  name: string;
  shippingAddress: string;
  phoneNumber: string;
  email: string;
  createdAt: string;
}

// 회원 생성 요청 타입
export interface CreateMemberRequest {
  name: string;
  shippingAddress: string;
  phoneNumber: string;
  email: string;
}

// 회원 수정 요청 타입
export interface UpdateMemberRequest {
  name?: string;
  shippingAddress?: string;
  phoneNumber?: string;
  email?: string;
}

// 회원 검색 조건 타입
export interface MemberSearchCondition {
  name?: string;
  email?: string;
  phoneNumber?: string;
}