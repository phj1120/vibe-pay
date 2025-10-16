import React from 'react';
import { Member } from '@/types/member';
import Card from '@/components/ui/Card';
import { formatDate, formatPhoneNumber } from '@/lib/formatters';
import { cn } from '@/lib/utils';

// 회원 카드 props 타입
export interface MemberCardProps {
  member: Member;
  onClick?: () => void;
  showActions?: boolean;
  onEdit?: (member: Member) => void;
  onDelete?: (member: Member) => void;
  className?: string;
}

// 사용자 아이콘 컴포넌트
const UserIcon: React.FC = () => (
  <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
    />
  </svg>
);

// 이메일 아이콘 컴포넌트
const EmailIcon: React.FC = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
    />
  </svg>
);

// 전화 아이콘 컴포넌트
const PhoneIcon: React.FC = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
    />
  </svg>
);

// 위치 아이콘 컴포넌트
const LocationIcon: React.FC = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
    />
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
    />
  </svg>
);

// 편집 아이콘 컴포넌트
const EditIcon: React.FC = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
    />
  </svg>
);

// 삭제 아이콘 컴포넌트
const DeleteIcon: React.FC = () => (
  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
    />
  </svg>
);

// 회원 카드 컴포넌트
const MemberCard: React.FC<MemberCardProps> = ({
  member,
  onClick,
  showActions = false,
  onEdit,
  onDelete,
  className,
}) => {
  // 액션 버튼 클릭 시 이벤트 전파 방지
  const handleActionClick = (e: React.MouseEvent, action: () => void) => {
    e.stopPropagation();
    action();
  };

  return (
    <Card
      hoverable={!!onClick}
      className={cn('transition-all duration-200', className)}
      onClick={onClick}
    >
      <Card.Header
        title={
          <div className="flex items-center space-x-3">
            <div className="flex-shrink-0 w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
              <UserIcon />
            </div>
            <div className="min-w-0">
              <h3 className="text-lg font-semibold text-gray-900 truncate">
                {member.name}
              </h3>
              <p className="text-sm text-gray-500">
                회원 ID: {member.memberId}
              </p>
            </div>
          </div>
        }
        action={
          showActions && (onEdit || onDelete) && (
            <div className="flex items-center space-x-2">
              {onEdit && (
                <button
                  type="button"
                  onClick={(e) => handleActionClick(e, () => onEdit(member))}
                  className="p-1 text-gray-400 hover:text-blue-600 transition-colors"
                  aria-label="회원 정보 수정"
                >
                  <EditIcon />
                </button>
              )}
              {onDelete && (
                <button
                  type="button"
                  onClick={(e) => handleActionClick(e, () => onDelete(member))}
                  className="p-1 text-gray-400 hover:text-red-600 transition-colors"
                  aria-label="회원 삭제"
                >
                  <DeleteIcon />
                </button>
              )}
            </div>
          )
        }
      />

      <Card.Body>
        <div className="space-y-3">
          {/* 이메일 */}
          <div className="flex items-center space-x-2 text-sm">
            <EmailIcon />
            <span className="text-gray-600">이메일:</span>
            <span className="text-gray-900 truncate">{member.email}</span>
          </div>

          {/* 전화번호 */}
          <div className="flex items-center space-x-2 text-sm">
            <PhoneIcon />
            <span className="text-gray-600">전화:</span>
            <span className="text-gray-900">{formatPhoneNumber(member.phoneNumber)}</span>
          </div>

          {/* 주소 */}
          <div className="flex items-start space-x-2 text-sm">
            <LocationIcon />
            <span className="text-gray-600 flex-shrink-0">주소:</span>
            <span className="text-gray-900 break-words">{member.shippingAddress}</span>
          </div>
        </div>
      </Card.Body>

      <Card.Footer>
        <div className="flex items-center justify-between text-sm text-gray-500">
          <span>가입일: {formatDate(member.createdAt)}</span>
          {onClick && (
            <span className="text-blue-600 hover:text-blue-700">
              자세히 보기 →
            </span>
          )}
        </div>
      </Card.Footer>
    </Card>
  );
};

export default MemberCard;