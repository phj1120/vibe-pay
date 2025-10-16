import React from 'react';
import { cn } from '@/lib/utils';

// 카드 variant 타입
export type CardVariant = 'default' | 'bordered' | 'elevated' | 'outlined';

// 카드 padding 타입
export type CardPadding = 'none' | 'sm' | 'md' | 'lg';

// 카드 props 타입
export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: CardVariant;
  padding?: CardPadding;
  hoverable?: boolean;
  children: React.ReactNode;
}

// 카드 헤더 props 타입
export interface CardHeaderProps extends React.HTMLAttributes<HTMLDivElement> {
  title?: string;
  subtitle?: string;
  action?: React.ReactNode;
  children?: React.ReactNode;
}

// 카드 바디 props 타입
export interface CardBodyProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
}

// 카드 푸터 props 타입
export interface CardFooterProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
}

// 카드 variant 스타일 맵
const cardVariants: Record<CardVariant, string> = {
  default: 'bg-white border border-gray-200',
  bordered: 'bg-white border-2 border-gray-300',
  elevated: 'bg-white shadow-lg border border-gray-100',
  outlined: 'bg-transparent border-2 border-gray-300',
};

// 카드 padding 스타일 맵
const cardPaddings: Record<CardPadding, string> = {
  none: '',
  sm: 'p-3',
  md: 'p-4',
  lg: 'p-6',
};

// 메인 카드 컴포넌트
const Card: React.FC<CardProps> = ({
  variant = 'default',
  padding = 'md',
  hoverable = false,
  className,
  children,
  ...props
}) => {
  const baseClasses = 'rounded-lg transition-shadow';
  const variantClasses = cardVariants[variant];
  const paddingClasses = cardPaddings[padding];
  const hoverClasses = hoverable ? 'hover:shadow-md cursor-pointer' : '';

  const cardClasses = cn(
    baseClasses,
    variantClasses,
    paddingClasses,
    hoverClasses,
    className
  );

  return (
    <div className={cardClasses} {...props}>
      {children}
    </div>
  );
};

// 카드 헤더 컴포넌트
const CardHeader: React.FC<CardHeaderProps> = ({
  title,
  subtitle,
  action,
  className,
  children,
  ...props
}) => {
  return (
    <div
      className={cn('flex items-center justify-between pb-3 border-b border-gray-200', className)}
      {...props}
    >
      <div className="flex-1 min-w-0">
        {title && (
          <h3 className="text-lg font-semibold text-gray-900 truncate">
            {title}
          </h3>
        )}
        {subtitle && (
          <p className="text-sm text-gray-500 mt-1">
            {subtitle}
          </p>
        )}
        {children}
      </div>
      {action && (
        <div className="flex-shrink-0 ml-4">
          {action}
        </div>
      )}
    </div>
  );
};

// 카드 바디 컴포넌트
const CardBody: React.FC<CardBodyProps> = ({
  className,
  children,
  ...props
}) => {
  return (
    <div className={cn('py-3', className)} {...props}>
      {children}
    </div>
  );
};

// 카드 푸터 컴포넌트
const CardFooter: React.FC<CardFooterProps> = ({
  className,
  children,
  ...props
}) => {
  return (
    <div
      className={cn('pt-3 border-t border-gray-200', className)}
      {...props}
    >
      {children}
    </div>
  );
};

// 서브 컴포넌트들을 메인 컴포넌트에 연결
Card.Header = CardHeader;
Card.Body = CardBody;
Card.Footer = CardFooter;

export default Card;
export { CardHeader, CardBody, CardFooter };