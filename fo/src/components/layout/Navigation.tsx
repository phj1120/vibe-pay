'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';

// 네비게이션 아이템 타입
export interface NavigationItem {
  href: string;
  label: string;
  icon?: React.ReactNode;
  badge?: string | number;
  children?: NavigationItem[];
}

// 네비게이션 props 타입
export interface NavigationProps {
  items: NavigationItem[];
  orientation?: 'horizontal' | 'vertical';
  className?: string;
}

// 네비게이션 아이템 컴포넌트
const NavigationItem: React.FC<{
  item: NavigationItem;
  isActive: boolean;
  orientation: 'horizontal' | 'vertical';
}> = ({ item, isActive, orientation }) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const hasChildren = item.children && item.children.length > 0;

  const linkClasses = cn(
    'flex items-center transition-colors font-medium',
    orientation === 'horizontal'
      ? 'px-3 py-2 rounded-md'
      : 'px-4 py-2 w-full',
    isActive
      ? 'text-blue-600 bg-blue-50'
      : 'text-gray-700 hover:text-blue-600 hover:bg-gray-50'
  );

  if (hasChildren) {
    return (
      <div className={orientation === 'vertical' ? 'w-full' : 'relative'}>
        <button
          type="button"
          className={linkClasses}
          onClick={() => setIsOpen(!isOpen)}
          aria-expanded={isOpen}
        >
          {item.icon && <span className="mr-2">{item.icon}</span>}
          <span>{item.label}</span>
          {item.badge && (
            <span className="ml-2 px-2 py-0.5 text-xs bg-red-500 text-white rounded-full">
              {item.badge}
            </span>
          )}
          <svg
            className={cn(
              'ml-1 h-4 w-4 transition-transform',
              isOpen && 'rotate-180'
            )}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 9l-7 7-7-7"
            />
          </svg>
        </button>

        {isOpen && (
          <div className={cn(
            orientation === 'horizontal'
              ? 'absolute top-full left-0 mt-1 w-48 bg-white border border-gray-200 rounded-md shadow-lg z-50'
              : 'pl-4 border-l border-gray-200 ml-4'
          )}>
            {item.children!.map((child) => (
              <NavigationItem
                key={child.href}
                item={child}
                isActive={false} // TODO: 하위 메뉴 active 상태 처리
                orientation={orientation}
              />
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <Link href={item.href} className={linkClasses}>
      {item.icon && <span className="mr-2">{item.icon}</span>}
      <span>{item.label}</span>
      {item.badge && (
        <span className="ml-2 px-2 py-0.5 text-xs bg-red-500 text-white rounded-full">
          {item.badge}
        </span>
      )}
    </Link>
  );
};

// 메인 네비게이션 컴포넌트
const Navigation: React.FC<NavigationProps> = ({
  items,
  orientation = 'horizontal',
  className,
}) => {
  const pathname = usePathname();

  const containerClasses = cn(
    'flex',
    orientation === 'horizontal' ? 'items-center space-x-1' : 'flex-col space-y-1',
    className
  );

  return (
    <nav className={containerClasses} role="navigation">
      {items.map((item) => {
        const isActive = pathname === item.href ||
          (item.href !== '/' && pathname.startsWith(item.href));

        return (
          <NavigationItem
            key={item.href}
            item={item}
            isActive={isActive}
            orientation={orientation}
          />
        );
      })}
    </nav>
  );
};

// 브레드크럼 컴포넌트
export const Breadcrumb: React.FC<{
  items: { href?: string; label: string }[];
  className?: string;
}> = ({ items, className }) => {
  return (
    <nav
      className={cn('flex items-center space-x-2 text-sm', className)}
      aria-label="Breadcrumb"
    >
      {items.map((item, index) => (
        <React.Fragment key={index}>
          {index > 0 && (
            <svg
              className="h-4 w-4 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5l7 7-7 7"
              />
            </svg>
          )}
          {item.href ? (
            <Link
              href={item.href}
              className="text-gray-600 hover:text-blue-600 transition-colors"
            >
              {item.label}
            </Link>
          ) : (
            <span className="text-gray-900 font-medium">{item.label}</span>
          )}
        </React.Fragment>
      ))}
    </nav>
  );
};

// 사이드바 네비게이션 컴포넌트
export const SidebarNavigation: React.FC<{
  items: NavigationItem[];
  className?: string;
}> = ({ items, className }) => {
  return (
    <aside className={cn('w-64 bg-white border-r border-gray-200', className)}>
      <div className="p-4">
        <Navigation items={items} orientation="vertical" />
      </div>
    </aside>
  );
};

// 탭 네비게이션 컴포넌트
export const TabNavigation: React.FC<{
  items: NavigationItem[];
  className?: string;
}> = ({ items, className }) => {
  return (
    <div className={cn('border-b border-gray-200', className)}>
      <Navigation
        items={items}
        orientation="horizontal"
        className="space-x-8 px-4"
      />
    </div>
  );
};

export default Navigation;