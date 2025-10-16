'use client';

import React from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { formatCurrency, formatDateTime } from '@/lib/formatters';

// 주문 취소/환불 페이지 컴포넌트
export default function OrderReturnPage() {
  const searchParams = useSearchParams();
  const orderId = searchParams.get('orderId');
  const returnType = searchParams.get('type') || 'cancel'; // cancel | refund

  // TODO: 실제 주문 정보 조회
  const orderInfo = {
    orderId: orderId || 'ORD-001',
    orderDate: new Date().toISOString(),
    totalAmount: 50000,
    status: returnType === 'cancel' ? 'CANCELLED' : 'REFUNDED',
    returnDate: new Date().toISOString(),
    returnAmount: 50000,
    items: [
      { name: '상품 1', quantity: 2, price: 25000 },
    ],
  };

  const pageConfig = {
    cancel: {
      title: '주문이 취소되었습니다',
      description: '주문 취소가 완료되었습니다.',
      statusText: '취소 완료',
      amountLabel: '취소 금액',
      icon: (
        <svg className="w-8 h-8 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
      bgColor: 'bg-orange-100',
    },
    refund: {
      title: '환불이 완료되었습니다',
      description: '결제 취소 및 환불 처리가 완료되었습니다.',
      statusText: '환불 완료',
      amountLabel: '환불 금액',
      icon: (
        <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
      bgColor: 'bg-green-100',
    },
  };

  const config = pageConfig[returnType as keyof typeof pageConfig] || pageConfig.cancel;

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        {/* 상태 메시지 */}
        <div className="text-center mb-8">
          <div className={`w-16 h-16 ${config.bgColor} rounded-full flex items-center justify-center mx-auto mb-4`}>
            {config.icon}
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">{config.title}</h1>
          <p className="text-gray-600">{config.description}</p>
        </div>

        {/* 주문 정보 */}
        <Card className="mb-6">
          <Card.Header title="주문 정보" />
          <Card.Body>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-500">주문번호</label>
                  <p className="text-lg font-medium text-gray-900">{orderInfo.orderId}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">주문일시</label>
                  <p className="text-lg font-medium text-gray-900">
                    {formatDateTime(orderInfo.orderDate)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">처리일시</label>
                  <p className="text-lg font-medium text-gray-900">
                    {formatDateTime(orderInfo.returnDate)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">상태</label>
                  <p className="text-lg font-medium text-orange-600">{config.statusText}</p>
                </div>
              </div>

              <hr />

              {/* 주문 상품 */}
              <div>
                <h3 className="font-medium text-gray-900 mb-3">주문 상품</h3>
                <div className="space-y-2">
                  {orderInfo.items.map((item, index) => (
                    <div key={index} className="flex justify-between items-center py-2">
                      <div>
                        <p className="font-medium text-gray-900">{item.name}</p>
                        <p className="text-sm text-gray-500">
                          {formatCurrency(item.price)} × {item.quantity}개
                        </p>
                      </div>
                      <p className="font-medium text-gray-900">
                        {formatCurrency(item.price * item.quantity)}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </Card.Body>
        </Card>

        {/* 취소/환불 정보 */}
        <Card className="mb-8">
          <Card.Header title={returnType === 'cancel' ? '취소 정보' : '환불 정보'} />
          <Card.Body>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-gray-500">{config.amountLabel}</label>
                <p className="text-xl font-bold text-blue-600">
                  {formatCurrency(orderInfo.returnAmount)}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">처리 상태</label>
                <p className="text-lg font-medium text-green-600">완료</p>
              </div>
              {returnType === 'refund' && (
                <>
                  <div>
                    <label className="text-sm font-medium text-gray-500">환불 방법</label>
                    <p className="text-lg font-medium text-gray-900">원결제수단</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-gray-500">환불 예정일</label>
                    <p className="text-lg font-medium text-gray-900">
                      {new Date(Date.now() + 3 * 24 * 60 * 60 * 1000).toLocaleDateString('ko-KR')}
                    </p>
                  </div>
                </>
              )}
            </div>
          </Card.Body>
        </Card>

        {/* 안내 메시지 */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-8">
          <div className="flex items-start">
            <svg className="w-5 h-5 text-blue-600 mt-0.5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div>
              <h4 className="font-medium text-blue-900 mb-1">
                {returnType === 'cancel' ? '취소' : '환불'} 안내
              </h4>
              <ul className="text-sm text-blue-800 space-y-1">
                {returnType === 'cancel' ? (
                  <>
                    <li>• 주문이 성공적으로 취소되었습니다.</li>
                    <li>• 결제가 진행되지 않았으므로 별도 환불 절차는 없습니다.</li>
                    <li>• 취소 확인 이메일이 발송됩니다.</li>
                  </>
                ) : (
                  <>
                    <li>• 환불 처리가 완료되었습니다.</li>
                    <li>• 실제 환불은 결제수단에 따라 1-3영업일 소요됩니다.</li>
                    <li>• 환불 완료 시 별도 알림을 드립니다.</li>
                    <li>• 카드 결제의 경우 다음 결제일에 차감됩니다.</li>
                  </>
                )}
                <li>• 문의사항이 있으시면 고객센터로 연락해주세요.</li>
              </ul>
            </div>
          </div>
        </div>

        {/* 액션 버튼 */}
        <div className="flex flex-col sm:flex-row gap-4">
          <Link href="/orders" className="flex-1">
            <Button variant="outline" fullWidth>
              주문 내역 보기
            </Button>
          </Link>
          <Link href="/products" className="flex-1">
            <Button fullWidth>
              쇼핑 계속하기
            </Button>
          </Link>
        </div>

        {/* 고객센터 정보 */}
        <div className="text-center mt-8 pt-8 border-t border-gray-200">
          <p className="text-sm text-gray-500 mb-2">문의사항이 있으시면 언제든 연락해주세요</p>
          <div className="space-y-1">
            <p className="text-sm font-medium text-gray-700">
              고객센터: 1588-0000 (평일 09:00~18:00)
            </p>
            <p className="text-sm text-gray-500">
              이메일: support@vibepay.com
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}