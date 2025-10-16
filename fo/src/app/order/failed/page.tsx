'use client';

import React from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';

// 주문 실패 페이지 컴포넌트
export default function OrderFailedPage() {
  const searchParams = useSearchParams();
  const errorCode = searchParams.get('errorCode');
  const errorMessage = searchParams.get('errorMessage');

  // 에러 타입별 메시지
  const getErrorInfo = () => {
    switch (errorCode) {
      case 'PAYMENT_FAILED':
        return {
          title: '결제에 실패했습니다',
          message: '결제 처리 중 오류가 발생했습니다.',
          suggestions: [
            '카드 정보를 다시 확인해주세요',
            '카드 한도를 확인해주세요',
            '다른 결제 수단을 시도해보세요',
          ],
        };
      case 'INSUFFICIENT_FUNDS':
        return {
          title: '잔액이 부족합니다',
          message: '결제할 금액이 부족합니다.',
          suggestions: [
            '계좌 잔액을 확인해주세요',
            '다른 카드로 시도해보세요',
            '결제 금액을 확인해주세요',
          ],
        };
      case 'CARD_ERROR':
        return {
          title: '카드 오류가 발생했습니다',
          message: '카드 정보에 오류가 있습니다.',
          suggestions: [
            '카드번호를 다시 확인해주세요',
            '유효기간을 확인해주세요',
            'CVV 번호를 확인해주세요',
          ],
        };
      case 'TIMEOUT':
        return {
          title: '결제 시간이 초과되었습니다',
          message: '결제 처리 시간이 초과되었습니다.',
          suggestions: [
            '네트워크 연결을 확인해주세요',
            '잠시 후 다시 시도해주세요',
            '페이지를 새로고침해주세요',
          ],
        };
      default:
        return {
          title: '주문 처리에 실패했습니다',
          message: errorMessage || '알 수 없는 오류가 발생했습니다.',
          suggestions: [
            '입력 정보를 다시 확인해주세요',
            '잠시 후 다시 시도해주세요',
            '고객센터로 문의해주세요',
          ],
        };
    }
  };

  const errorInfo = getErrorInfo();

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        {/* 실패 메시지 */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">{errorInfo.title}</h1>
          <p className="text-gray-600">{errorInfo.message}</p>
        </div>

        {/* 오류 정보 */}
        <Card className="mb-6">
          <Card.Header title="오류 정보" />
          <Card.Body>
            <div className="space-y-4">
              {errorCode && (
                <div>
                  <label className="text-sm font-medium text-gray-500">오류 코드</label>
                  <p className="text-lg font-medium text-red-600">{errorCode}</p>
                </div>
              )}
              <div>
                <label className="text-sm font-medium text-gray-500">발생 시간</label>
                <p className="text-lg font-medium text-gray-900">
                  {new Date().toLocaleString('ko-KR')}
                </p>
              </div>
              {errorMessage && (
                <div>
                  <label className="text-sm font-medium text-gray-500">상세 메시지</label>
                  <p className="text-lg text-gray-700">{errorMessage}</p>
                </div>
              )}
            </div>
          </Card.Body>
        </Card>

        {/* 해결 방법 */}
        <Card className="mb-8">
          <Card.Header title="해결 방법" />
          <Card.Body>
            <div className="space-y-3">
              {errorInfo.suggestions.map((suggestion, index) => (
                <div key={index} className="flex items-start">
                  <div className="w-2 h-2 bg-blue-500 rounded-full mt-2 mr-3 flex-shrink-0"></div>
                  <p className="text-gray-700">{suggestion}</p>
                </div>
              ))}
            </div>
          </Card.Body>
        </Card>

        {/* 안내 메시지 */}
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-8">
          <div className="flex items-start">
            <svg className="w-5 h-5 text-yellow-600 mt-0.5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
            <div>
              <h4 className="font-medium text-yellow-900 mb-1">주의사항</h4>
              <ul className="text-sm text-yellow-800 space-y-1">
                <li>• 결제가 실패했으므로 실제 결제는 이루어지지 않았습니다.</li>
                <li>• 카드사에서 일시적으로 승인 요청이 보일 수 있으나 자동 취소됩니다.</li>
                <li>• 문제가 지속될 경우 고객센터로 문의해주세요.</li>
              </ul>
            </div>
          </div>
        </div>

        {/* 액션 버튼 */}
        <div className="flex flex-col sm:flex-row gap-4">
          <Link href="/order" className="flex-1">
            <Button fullWidth>
              다시 주문하기
            </Button>
          </Link>
          <Link href="/products" className="flex-1">
            <Button variant="outline" fullWidth>
              쇼핑 계속하기
            </Button>
          </Link>
        </div>

        {/* 고객센터 정보 */}
        <div className="text-center mt-8 pt-8 border-t border-gray-200">
          <p className="text-sm text-gray-500 mb-2">문제가 지속될 경우 고객센터로 문의해주세요</p>
          <div className="space-y-1">
            <p className="text-sm font-medium text-gray-700">
              고객센터: 1588-0000 (평일 09:00~18:00)
            </p>
            <p className="text-sm text-gray-500">
              이메일: support@vibepay.com
            </p>
          </div>
        </div>

        {/* 자주 묻는 질문 링크 */}
        <div className="text-center mt-6">
          <Link href="/faq" className="text-sm text-blue-600 hover:text-blue-700 underline">
            자주 묻는 질문 보기
          </Link>
        </div>
      </div>
    </div>
  );
}