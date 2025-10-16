'use client';

import React from 'react';
import { Member } from '@/types/member';
import { PageResponse } from '@/types/api';
import MemberCard from './MemberCard';
import Loading from '@/components/ui/Loading';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import { cn } from '@/lib/utils';

// 회원 목록 props 타입
export interface MemberListProps {
  members?: PageResponse<Member>;
  loading?: boolean;
  error?: string;
  onRefresh?: () => void;
  onSearch?: (keyword: string) => void;
  onLoadMore?: () => void;
  onMemberClick?: (member: Member) => void;
  className?: string;
}

// 검색 아이콘 컴포넌트
const SearchIcon: React.FC = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
    />
  </svg>
);

// 새로고침 아이콘 컴포넌트
const RefreshIcon: React.FC = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
    />
  </svg>
);

// 회원 목록 컴포넌트
const MemberList: React.FC<MemberListProps> = ({
  members,
  loading = false,
  error,
  onRefresh,
  onSearch,
  onLoadMore,
  onMemberClick,
  className,
}) => {
  const [searchKeyword, setSearchKeyword] = React.useState('');

  // 검색 처리
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (onSearch) {
      onSearch(searchKeyword.trim());
    }
  };

  // 검색 키워드 변경 처리
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(e.target.value);
  };

  // 엔터 키 처리
  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch(e as any);
    }
  };

  return (
    <div className={cn('space-y-4', className)}>
      {/* 헤더 */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">회원 목록</h2>
          {members && (
            <p className="text-sm text-gray-600 mt-1">
              총 {members.totalElements}명의 회원
            </p>
          )}
        </div>

        <div className="flex items-center gap-2">
          {/* 새로고침 버튼 */}
          {onRefresh && (
            <Button
              variant="outline"
              size="sm"
              onClick={onRefresh}
              disabled={loading}
            >
              <RefreshIcon />
            </Button>
          )}
        </div>
      </div>

      {/* 검색 */}
      {onSearch && (
        <form onSubmit={handleSearch} className="flex gap-2">
          <Input
            type="text"
            placeholder="이름, 이메일, 전화번호로 검색..."
            value={searchKeyword}
            onChange={handleSearchChange}
            onKeyPress={handleKeyPress}
            className="flex-1"
            rightIcon={<SearchIcon />}
          />
          <Button type="submit" disabled={loading}>
            검색
          </Button>
        </form>
      )}

      {/* 에러 상태 */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex items-center">
            <svg
              className="h-5 w-5 text-red-400 mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="text-sm text-red-800">{error}</p>
          </div>
        </div>
      )}

      {/* 로딩 상태 */}
      {loading && !members && (
        <div className="space-y-4">
          <Loading.SkeletonList count={5} height="6rem" />
        </div>
      )}

      {/* 회원 목록 */}
      {members && members.content.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {members.content.map((member) => (
            <MemberCard
              key={member.memberId}
              member={member}
              onClick={() => onMemberClick?.(member)}
              className="cursor-pointer hover:shadow-md transition-shadow"
            />
          ))}
        </div>
      )}

      {/* 빈 상태 */}
      {members && members.content.length === 0 && !loading && (
        <div className="text-center py-12">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM9 9a2 2 0 11-4 0 2 2 0 014 0z"
            />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900">회원이 없습니다</h3>
          <p className="mt-1 text-sm text-gray-500">
            {searchKeyword ? '검색 조건에 맞는 회원이 없습니다.' : '등록된 회원이 없습니다.'}
          </p>
        </div>
      )}

      {/* 더 보기 버튼 */}
      {members && !members.last && onLoadMore && (
        <div className="text-center">
          <Button
            variant="outline"
            onClick={onLoadMore}
            loading={loading}
            disabled={loading}
          >
            더 보기
          </Button>
        </div>
      )}

      {/* 페이지 정보 */}
      {members && members.content.length > 0 && (
        <div className="text-center text-sm text-gray-500">
          {members.number + 1} / {members.totalPages} 페이지
          ({members.content.length} / {members.totalElements} 회원)
        </div>
      )}
    </div>
  );
};

export default MemberList;