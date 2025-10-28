import { apiClient } from "./api-client";
import type {
  MemberRegisterRequest,
  MemberLoginRequest,
  MemberLoginResponse,
  MemberInfoResponse,
} from "@/types/member";

export const memberApi = {
  // 회원 가입
  register: async (data: MemberRegisterRequest): Promise<void> => {
    await apiClient<void>("/api/members/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // 로그인
  login: async (data: MemberLoginRequest): Promise<MemberLoginResponse> => {
    return await apiClient<MemberLoginResponse>("/api/members/login", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  // 회원 정보 조회
  getMyInfo: async (): Promise<MemberInfoResponse> => {
    return await apiClient<MemberInfoResponse>("/api/members/me", {
      method: "GET",
    });
  },
};
