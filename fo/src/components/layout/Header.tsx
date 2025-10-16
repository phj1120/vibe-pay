'use client';

import React from 'react';
import Link from 'next/link';
import { useCart } from '@/context/CartContext';
import { cn } from '@/lib/utils';

// 헤더 props 타입
export interface HeaderProps {
  className?: string;
}

// 쇼핑카트 아이콘 컴포넌트
const CartIcon: React.FC<{ itemCount: number }> = ({ itemCount }) => (
  <div className="relative">
    <svg
      className="h-6 w-6"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l-1.5 6m4.5-6h6m-6 0v6m6-6v6"
      />
    </svg>
    {itemCount > 0 && (
      <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
        {itemCount > 99 ? '99+' : itemCount}
      </span>
    )}
  </div>
);

// 메뉴 아이콘 컴포넌트
const MenuIcon: React.FC = () => (
  <svg
    className="h-6 w-6"
    fill="none"
    stroke="currentColor"
    viewBox="0 0 24 24"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M4 6h16M4 12h16M4 18h16"
    />
  </svg>
);

// 헤더 컴포넌트
const Header: React.FC<HeaderProps> = ({ className }) => {
  const { state: cartState } = useCart();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = React.useState(false);

  // 네비게이션 메뉴 항목들
  const navigationItems = [
    { href: '/', label: '홈' },
    { href: '/products', label: '상품' },
    { href: '/members', label: '회원' },
    { href: '/order', label: '주문' },
  ];

  return (
    <header className={cn('bg-white border-b border-gray-200 sticky top-0 z-40', className)}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* 로고 */}
          <div className="flex-shrink-0">
            <Link
              href="/"
              className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition-colors"
            >
              Vibe Pay
            </Link>
          </div>

          {/* 데스크톱 네비게이션 */}
          <nav className="hidden md:flex items-center space-x-8">
            {navigationItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="text-gray-700 hover:text-blue-600 font-medium transition-colors"
              >
                {item.label}
              </Link>
            ))}
          </nav>

          {/* 우측 액션 버튼들 */}
          <div className="flex items-center space-x-4">
            {/* 장바구니 */}
            <Link
              href="/cart"
              className="text-gray-700 hover:text-blue-600 transition-colors"
              aria-label={`장바구니 (${cartState.totalItems}개 상품)`}
            >
              <CartIcon itemCount={cartState.totalItems} />
            </Link>

            {/* TODO: 인증 기능 활성화 시 로그인/로그아웃 버튼 */}
            {/* <div className="hidden sm:flex items-center space-x-2">
              <Link
                href="/login"
                className="text-gray-700 hover:text-blue-600 font-medium transition-colors"
              >
                로그인
              </Link>
              <span className="text-gray-300">|</span>
              <Link
                href="/signup"
                className="text-gray-700 hover:text-blue-600 font-medium transition-colors"
              >
                회원가입
              </Link>
            </div> */}

            {/* 모바일 메뉴 버튼 */}
            <button
              type="button"
              className="md:hidden text-gray-700 hover:text-blue-600 transition-colors"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              aria-label="메뉴 열기"
            >
              <MenuIcon />
            </button>
          </div>
        </div>

        {/* 모바일 네비게이션 */}
        {isMobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 py-4">
            <nav className="flex flex-col space-y-3">
              {navigationItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="text-gray-700 hover:text-blue-600 font-medium transition-colors px-2 py-1"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  {item.label}
                </Link>
              ))}

              {/* TODO: 인증 기능 활성화 시 모바일 로그인 링크 */}
              {/* <div className="border-t border-gray-200 pt-3 mt-3">
                <Link
                  href="/login"
                  className="text-gray-700 hover:text-blue-600 font-medium transition-colors px-2 py-1 block"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  로그인
                </Link>
                <Link
                  href="/signup"
                  className="text-gray-700 hover:text-blue-600 font-medium transition-colors px-2 py-1 block"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  회원가입
                </Link>
              </div> */}
            </nav>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;