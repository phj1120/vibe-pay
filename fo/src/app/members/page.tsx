'use client';

import React from 'react';
import { Member } from '@/types/member';
import { PageResponse } from '@/types/api';
import MemberList from '@/components/member/MemberList';
import { getMembers, searchMembers } from '@/lib/api/members';

// 회원 목록 페이지 컴포넌트
export default function MembersPage() {
  const [members, setMembers] = React.useState<PageResponse<Member>>();
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string>();

  // 회원 목록 조회
  const fetchMembers = React.useCallback(async (params?: any) => {
    try {
      setLoading(true);
      setError(undefined);
      const data = await getMembers(params);
      setMembers(data);
    } catch (err) {
      setError('회원 목록을 불러오는데 실패했습니다.');
      console.error('Failed to fetch members:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  // 회원 검색
  const handleSearch = React.useCallback(async (keyword: string) => {
    if (!keyword.trim()) {
      await fetchMembers();
      return;
    }

    try {
      setLoading(true);
      setError(undefined);
      const data = await searchMembers({
        name: keyword,
        email: keyword,
        phoneNumber: keyword,
      });
      setMembers(data);
    } catch (err) {
      setError('회원 검색에 실패했습니다.');
      console.error('Failed to search members:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  // 회원 클릭 시 상세 페이지로 이동
  const handleMemberClick = (member: Member) => {
    window.location.href = `/members/${member.memberId}`;
  };

  // 더 보기 (페이지네이션)
  const handleLoadMore = React.useCallback(async () => {
    if (!members || members.last) return;

    try {
      setLoading(true);
      const nextPage = await getMembers({
        page: members.number + 1,
        size: members.size,
      });

      setMembers(prev => prev ? {
        ...nextPage,
        content: [...prev.content, ...nextPage.content],
      } : nextPage);
    } catch (err) {
      setError('추가 회원 목록을 불러오는데 실패했습니다.');
      console.error('Failed to load more members:', err);
    } finally {
      setLoading(false);
    }
  }, [members]);

  // 컴포넌트 마운트 시 회원 목록 조회
  React.useEffect(() => {
    fetchMembers();
  }, [fetchMembers]);

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">회원 관리</h1>
        <p className="text-gray-600 mt-2">
          등록된 회원들의 정보를 조회하고 관리할 수 있습니다.
        </p>
      </div>

      <MemberList
        members={members}
        loading={loading}
        error={error}
        onRefresh={() => fetchMembers()}
        onSearch={handleSearch}
        onLoadMore={handleLoadMore}
        onMemberClick={handleMemberClick}
      />
    </div>
  );
}