export interface MemberRegisterRequest {
  memberName: string;
  phone: string;
  email: string;
  password: string;
}

export interface MemberLoginRequest {
  email: string;
  password: string;
}

export interface MemberLoginResponse {
  accessToken: string;
  refreshToken: string;
}

export interface MemberInfoResponse {
  memberName: string;
  phone: string;
  email: string;
  memberStatusCode: string;
}
