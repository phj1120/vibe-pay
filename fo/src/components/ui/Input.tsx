import React from 'react';
import { cn } from '@/lib/utils';

// 인풋 타입
export type InputType = 'text' | 'email' | 'password' | 'number' | 'tel' | 'url' | 'search';

// 인풋 size 타입
export type InputSize = 'sm' | 'md' | 'lg';

// 인풋 props 타입
export interface InputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'> {
  label?: string;
  error?: string;
  helperText?: string;
  size?: InputSize;
  fullWidth?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

// 인풋 size 스타일 맵
const inputSizes: Record<InputSize, string> = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-3 py-2 text-base',
  lg: 'px-4 py-3 text-lg',
};

const Input: React.FC<InputProps> = ({
  label,
  error,
  helperText,
  size = 'md',
  fullWidth = false,
  leftIcon,
  rightIcon,
  className,
  disabled,
  required,
  ...props
}) => {
  const baseClasses = 'border rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:bg-gray-50 disabled:text-gray-500 disabled:cursor-not-allowed';

  const stateClasses = error
    ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
    : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500';

  const sizeClasses = inputSizes[size];
  const widthClasses = fullWidth ? 'w-full' : '';

  const inputClasses = cn(
    baseClasses,
    stateClasses,
    sizeClasses,
    widthClasses,
    leftIcon && 'pl-10',
    rightIcon && 'pr-10',
    className
  );

  const inputId = props.id || `input-${Math.random().toString(36).substr(2, 9)}`;

  return (
    <div className={cn('flex flex-col', fullWidth && 'w-full')}>
      {label && (
        <label
          htmlFor={inputId}
          className={cn(
            'mb-1 text-sm font-medium text-gray-700',
            disabled && 'text-gray-500'
          )}
        >
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}

      <div className="relative">
        {leftIcon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <div className="text-gray-400">{leftIcon}</div>
          </div>
        )}

        <input
          {...props}
          id={inputId}
          className={inputClasses}
          disabled={disabled}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={
            error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined
          }
        />

        {rightIcon && (
          <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
            <div className={cn('text-gray-400', error && 'text-red-400')}>
              {rightIcon}
            </div>
          </div>
        )}
      </div>

      {error && (
        <p
          id={`${inputId}-error`}
          className="mt-1 text-sm text-red-600"
          role="alert"
        >
          {error}
        </p>
      )}

      {helperText && !error && (
        <p
          id={`${inputId}-helper`}
          className="mt-1 text-sm text-gray-500"
        >
          {helperText}
        </p>
      )}
    </div>
  );
};

export default Input;