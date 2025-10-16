'use client';

import React, { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { cn } from '@/lib/utils';

// 모달 size 타입
export type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'full';

// 모달 props 타입
export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  size?: ModalSize;
  closeOnOverlayClick?: boolean;
  closeOnEscape?: boolean;
  showCloseButton?: boolean;
  children: React.ReactNode;
  className?: string;
}

// 모달 헤더 props 타입
export interface ModalHeaderProps {
  title?: string;
  onClose?: () => void;
  showCloseButton?: boolean;
  children?: React.ReactNode;
  className?: string;
}

// 모달 바디 props 타입
export interface ModalBodyProps {
  children: React.ReactNode;
  className?: string;
}

// 모달 푸터 props 타입
export interface ModalFooterProps {
  children: React.ReactNode;
  className?: string;
}

// 모달 size 스타일 맵
const modalSizes: Record<ModalSize, string> = {
  sm: 'max-w-md',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
  xl: 'max-w-4xl',
  full: 'max-w-full m-4',
};

// Close 아이콘 컴포넌트
const CloseIcon: React.FC = () => (
  <svg
    className="h-5 w-5"
    fill="none"
    stroke="currentColor"
    viewBox="0 0 24 24"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M6 18L18 6M6 6l12 12"
    />
  </svg>
);

// 메인 모달 컴포넌트
const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  size = 'md',
  closeOnOverlayClick = true,
  closeOnEscape = true,
  showCloseButton = true,
  children,
  className,
}) => {
  // ESC 키 이벤트 처리
  useEffect(() => {
    if (!closeOnEscape) return;

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, onClose, closeOnEscape]);

  // 바디 스크롤 제어
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }

    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const sizeClasses = modalSizes[size];

  const modalContent = (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* 오버레이 */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={closeOnOverlayClick ? onClose : undefined}
        aria-hidden="true"
      />

      {/* 모달 컨테이너 */}
      <div
        className={cn(
          'relative bg-white rounded-lg shadow-xl max-h-full overflow-y-auto w-full mx-4',
          sizeClasses,
          className
        )}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? 'modal-title' : undefined}
      >
        {/* 기본 헤더 (title이 있고 children에 헤더가 없는 경우) */}
        {title && !React.Children.toArray(children).some(child =>
          React.isValidElement(child) && child.type === ModalHeader
        ) && (
          <ModalHeader
            title={title}
            onClose={onClose}
            showCloseButton={showCloseButton}
          />
        )}

        {children}
      </div>
    </div>
  );

  // Portal을 사용하여 body에 렌더링
  return typeof document !== 'undefined'
    ? createPortal(modalContent, document.body)
    : null;
};

// 모달 헤더 컴포넌트
const ModalHeader: React.FC<ModalHeaderProps> = ({
  title,
  onClose,
  showCloseButton = true,
  children,
  className,
}) => {
  return (
    <div className={cn('flex items-center justify-between p-6 border-b border-gray-200', className)}>
      <div className="flex-1">
        {title && (
          <h2 id="modal-title" className="text-xl font-semibold text-gray-900">
            {title}
          </h2>
        )}
        {children}
      </div>
      {showCloseButton && onClose && (
        <button
          type="button"
          className="ml-4 text-gray-400 hover:text-gray-600 transition-colors"
          onClick={onClose}
          aria-label="모달 닫기"
        >
          <CloseIcon />
        </button>
      )}
    </div>
  );
};

// 모달 바디 컴포넌트
const ModalBody: React.FC<ModalBodyProps> = ({ children, className }) => {
  return (
    <div className={cn('p-6', className)}>
      {children}
    </div>
  );
};

// 모달 푸터 컴포넌트
const ModalFooter: React.FC<ModalFooterProps> = ({ children, className }) => {
  return (
    <div className={cn('flex items-center justify-end gap-3 p-6 border-t border-gray-200', className)}>
      {children}
    </div>
  );
};

// 서브 컴포넌트들을 메인 컴포넌트에 연결
Modal.Header = ModalHeader;
Modal.Body = ModalBody;
Modal.Footer = ModalFooter;

export default Modal;
export { ModalHeader, ModalBody, ModalFooter };