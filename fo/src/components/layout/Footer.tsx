import React from 'react';
import Link from 'next/link';
import { cn } from '@/lib/utils';

// 푸터 props 타입
export interface FooterProps {
  className?: string;
}

// 푸터 컴포넌트
const Footer: React.FC<FooterProps> = ({ className }) => {
  const currentYear = new Date().getFullYear();

  // 푸터 링크 섹션들
  const footerSections = [
    {
      title: '서비스',
      links: [
        { href: '/products', label: '상품 보기' },
        { href: '/order', label: '주문하기' },
        { href: '/order/history', label: '주문 내역' },
      ],
    },
    {
      title: '고객지원',
      links: [
        { href: '/help', label: '도움말' },
        { href: '/contact', label: '문의하기' },
        { href: '/faq', label: '자주 묻는 질문' },
      ],
    },
    {
      title: '회사정보',
      links: [
        { href: '/about', label: '회사 소개' },
        { href: '/terms', label: '이용약관' },
        { href: '/privacy', label: '개인정보처리방침' },
      ],
    },
  ];

  return (
    <footer className={cn('bg-gray-50 border-t border-gray-200', className)}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 메인 푸터 콘텐츠 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* 브랜드 섹션 */}
          <div className="md:col-span-1">
            <Link
              href="/"
              className="text-xl font-bold text-blue-600 hover:text-blue-700 transition-colors"
            >
              Vibe Pay
            </Link>
            <p className="mt-2 text-sm text-gray-600">
              간편하고 안전한 온라인 결제 서비스
            </p>
            <div className="mt-4 text-sm text-gray-500">
              <p>사업자등록번호: 123-45-67890</p>
              <p>대표: 홍길동</p>
              <p>주소: 서울특별시 강남구 테헤란로 123</p>
            </div>
          </div>

          {/* 링크 섹션들 */}
          {footerSections.map((section) => (
            <div key={section.title} className="md:col-span-1">
              <h3 className="text-sm font-semibold text-gray-900 mb-3">
                {section.title}
              </h3>
              <ul className="space-y-2">
                {section.links.map((link) => (
                  <li key={link.href}>
                    <Link
                      href={link.href}
                      className="text-sm text-gray-600 hover:text-blue-600 transition-colors"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* 구분선 */}
        <div className="mt-8 pt-6 border-t border-gray-200">
          <div className="flex flex-col md:flex-row items-center justify-between">
            {/* 저작권 정보 */}
            <div className="text-sm text-gray-500">
              © {currentYear} Vibe Pay. All rights reserved.
            </div>

            {/* 결제 수단 아이콘들 */}
            <div className="flex items-center space-x-4 mt-4 md:mt-0">
              <span className="text-sm text-gray-500">결제 수단:</span>
              <div className="flex items-center space-x-2">
                {/* TODO: 실제 결제 수단 아이콘들로 교체 */}
                <div className="w-8 h-5 bg-blue-600 rounded text-white text-xs flex items-center justify-center">
                  VISA
                </div>
                <div className="w-8 h-5 bg-red-600 rounded text-white text-xs flex items-center justify-center">
                  MC
                </div>
                <div className="w-8 h-5 bg-yellow-500 rounded text-white text-xs flex items-center justify-center">
                  KB
                </div>
              </div>
            </div>
          </div>

          {/* 추가 법적 고지 */}
          <div className="mt-4 text-xs text-gray-400">
            <p>
              Vibe Pay는 통신판매중개업자이며, 통신판매의 당사자가 아닙니다.
              상품의 주문, 배송 및 환불 등과 관련한 의무와 책임은 각 판매자에게 있습니다.
            </p>
            <p className="mt-1">
              고객센터: 1588-0000 (평일 09:00~18:00, 토·일·공휴일 휴무)
            </p>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;