import apiClient from './client';
import { ApiResponse, PageRequest, PageResponse } from '@/types/api';
import { Member, CreateMemberRequest, UpdateMemberRequest, MemberSearchCondition } from '@/types/member';

// 회원 목록 조회
export const getMembers = async (
  params?: PageRequest & MemberSearchCondition
): Promise<PageResponse<Member>> => {
  const response = await apiClient.get<ApiResponse<PageResponse<Member>>>('/members', { params });
  return response.data.data;
};

// 회원 상세 조회
export const getMember = async (memberId: number): Promise<Member> => {
  const response = await apiClient.get<ApiResponse<Member>>(`/members/${memberId}`);
  return response.data.data;
};

// 회원 생성
export const createMember = async (memberData: CreateMemberRequest): Promise<Member> => {
  const response = await apiClient.post<ApiResponse<Member>>('/members', memberData);
  return response.data.data;
};

// 회원 수정
export const updateMember = async (
  memberId: number,
  memberData: UpdateMemberRequest
): Promise<Member> => {
  const response = await apiClient.put<ApiResponse<Member>>(`/members/${memberId}`, memberData);
  return response.data.data;
};

// 회원 삭제
export const deleteMember = async (memberId: number): Promise<void> => {
  await apiClient.delete(`/members/${memberId}`);
};

// 회원 검색
export const searchMembers = async (
  searchCondition: MemberSearchCondition,
  pageRequest?: PageRequest
): Promise<PageResponse<Member>> => {
  const params = { ...searchCondition, ...pageRequest };
  const response = await apiClient.get<ApiResponse<PageResponse<Member>>>('/members/search', { params });
  return response.data.data;
};