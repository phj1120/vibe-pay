"use client";

import type { MemberInfoResponse } from "@/types/member";

interface MemberInfoSectionProps {
  memberInfo: MemberInfoResponse | null;
}

export default function MemberInfoSection({ memberInfo }: MemberInfoSectionProps) {
  if (!memberInfo) {
    return (
      <div className="text-center py-12 text-gray-400 text-sm">
        회원 정보를 불러올 수 없습니다
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between py-4 border-b">
        <span className="text-sm text-gray-600">이름</span>
        <span className="text-sm">{memberInfo.memberName}</span>
      </div>

      <div className="flex justify-between py-4 border-b">
        <span className="text-sm text-gray-600">이메일</span>
        <span className="text-sm">{memberInfo.email}</span>
      </div>

      <div className="flex justify-between py-4 border-b">
        <span className="text-sm text-gray-600">전화번호</span>
        <span className="text-sm">{memberInfo.phone}</span>
      </div>

      <div className="flex justify-between py-4 border-b">
        <span className="text-sm text-gray-600">회원 상태</span>
        <span className="text-sm">
          {memberInfo.memberStatusCode === "001" ? "정상" : "탈퇴"}
        </span>
      </div>
    </div>
  );
}
