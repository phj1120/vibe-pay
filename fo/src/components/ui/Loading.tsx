import React from 'react';
import { cn } from '@/lib/utils';

// 로딩 타입
export type LoadingType = 'spinner' | 'dots' | 'pulse' | 'skeleton';

// 로딩 size 타입
export type LoadingSize = 'sm' | 'md' | 'lg';

// 로딩 props 타입
export interface LoadingProps {
  type?: LoadingType;
  size?: LoadingSize;
  message?: string;
  overlay?: boolean;
  className?: string;
}

// 스켈레톤 props 타입
export interface SkeletonProps {
  width?: string | number;
  height?: string | number;
  className?: string;
}

// 로딩 size 스타일 맵
const loadingSizes: Record<LoadingSize, { spinner: string; text: string }> = {
  sm: { spinner: 'h-4 w-4', text: 'text-sm' },
  md: { spinner: 'h-6 w-6', text: 'text-base' },
  lg: { spinner: 'h-8 w-8', text: 'text-lg' },
};

// 스피너 컴포넌트
const Spinner: React.FC<{ size: LoadingSize; className?: string }> = ({ size, className }) => {
  const { spinner } = loadingSizes[size];

  return (
    <svg
      className={cn('animate-spin', spinner, className)}
      fill="none"
      viewBox="0 0 24 24"
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="4"
      />
      <path
        className="opacity-75"
        fill="currentColor"
        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
      />
    </svg>
  );
};

// 점 로딩 컴포넌트
const Dots: React.FC<{ size: LoadingSize; className?: string }> = ({ size, className }) => {
  const dotSize = size === 'sm' ? 'h-1 w-1' : size === 'md' ? 'h-2 w-2' : 'h-3 w-3';

  return (
    <div className={cn('flex space-x-1', className)}>
      <div className={cn('bg-current rounded-full animate-bounce', dotSize)} style={{ animationDelay: '0ms' }} />
      <div className={cn('bg-current rounded-full animate-bounce', dotSize)} style={{ animationDelay: '150ms' }} />
      <div className={cn('bg-current rounded-full animate-bounce', dotSize)} style={{ animationDelay: '300ms' }} />
    </div>
  );
};

// 펄스 로딩 컴포넌트
const Pulse: React.FC<{ size: LoadingSize; className?: string }> = ({ size, className }) => {
  const { spinner } = loadingSizes[size];

  return (
    <div className={cn('bg-current rounded-full animate-pulse', spinner, className)} />
  );
};

// 스켈레톤 컴포넌트
const Skeleton: React.FC<SkeletonProps> = ({ width = '100%', height = '1rem', className }) => {
  return (
    <div
      className={cn('bg-gray-200 rounded animate-pulse', className)}
      style={{ width, height }}
    />
  );
};

// 메인 로딩 컴포넌트
const Loading: React.FC<LoadingProps> = ({
  type = 'spinner',
  size = 'md',
  message,
  overlay = false,
  className,
}) => {
  const { text } = loadingSizes[size];

  // 로딩 타입별 컴포넌트 렌더링
  const renderLoadingComponent = () => {
    switch (type) {
      case 'spinner':
        return <Spinner size={size} className="text-blue-600" />;
      case 'dots':
        return <Dots size={size} className="text-blue-600" />;
      case 'pulse':
        return <Pulse size={size} className="text-blue-600" />;
      case 'skeleton':
        return <Skeleton />;
      default:
        return <Spinner size={size} className="text-blue-600" />;
    }
  };

  const content = (
    <div className={cn('flex flex-col items-center justify-center', className)}>
      {type !== 'skeleton' && renderLoadingComponent()}
      {message && (
        <p className={cn('mt-2 text-gray-600', text)}>
          {message}
        </p>
      )}
    </div>
  );

  if (overlay) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-white bg-opacity-75">
        {content}
      </div>
    );
  }

  return content;
};

// 페이지 로딩 컴포넌트
const PageLoading: React.FC<{ message?: string }> = ({ message = '로딩 중...' }) => {
  return (
    <div className="flex items-center justify-center min-h-screen">
      <Loading type="spinner" size="lg" message={message} />
    </div>
  );
};

// 버튼 로딩 컴포넌트
const ButtonLoading: React.FC = () => {
  return <Spinner size="sm" className="text-current" />;
};

// 스켈레톤 목록 컴포넌트
const SkeletonList: React.FC<{ count?: number; height?: string }> = ({ count = 5, height = '4rem' }) => {
  return (
    <div className="space-y-3">
      {Array.from({ length: count }).map((_, index) => (
        <Skeleton key={index} height={height} />
      ))}
    </div>
  );
};

// 서브 컴포넌트들을 메인 컴포넌트에 연결
Loading.Page = PageLoading;
Loading.Button = ButtonLoading;
Loading.Skeleton = Skeleton;
Loading.SkeletonList = SkeletonList;

export default Loading;
export { Skeleton, PageLoading, ButtonLoading, SkeletonList };