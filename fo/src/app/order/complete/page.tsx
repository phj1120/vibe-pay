'use client';

import React from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { formatCurrency, formatDateTime } from '@/lib/formatters';

// 주문 완료 페이지 컴포넌트
export default function OrderCompletePage() {
  const searchParams = useSearchParams();
  const orderId = searchParams.get('orderId');
  const paymentId = searchParams.get('paymentId');

  // TODO: 실제 주문/결제 정보 조회
  const orderInfo = {
    orderId: orderId || 'ORD-001',
    orderDate: new Date().toISOString(),
    totalAmount: 50000,
    status: 'CONFIRMED',
    items: [
      { name: '상품 1', quantity: 2, price: 25000 },
    ],
  };

  const paymentInfo = {
    paymentId: paymentId || 'PAY-001',
    paymentMethod: 'CARD',
    pgCompany: 'INICIS',
    amount: 50000,
    status: 'COMPLETED',
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        {/* 성공 메시지 */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">주문이 완료되었습니다!</h1>
          <p className="text-gray-600">결제가 성공적으로 처리되었습니다.</p>
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
                  <label className="text-sm font-medium text-gray-500">주문상태</label>
                  <p className="text-lg font-medium text-green-600">{orderInfo.status}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">총 결제금액</label>
                  <p className="text-xl font-bold text-blue-600">
                    {formatCurrency(orderInfo.totalAmount)}
                  </p>
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

        {/* 결제 정보 */}
        <Card className="mb-8">
          <Card.Header title="결제 정보" />
          <Card.Body>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium text-gray-500">결제번호</label>
                <p className="text-lg font-medium text-gray-900">{paymentInfo.paymentId}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">결제방법</label>
                <p className="text-lg font-medium text-gray-900">
                  {paymentInfo.paymentMethod} ({paymentInfo.pgCompany})
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">결제금액</label>
                <p className="text-xl font-bold text-blue-600">
                  {formatCurrency(paymentInfo.amount)}
                </p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">결제상태</label>
                <p className="text-lg font-medium text-green-600">{paymentInfo.status}</p>
              </div>
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
              <h4 className="font-medium text-blue-900 mb-1">주문 완료 안내</h4>
              <ul className="text-sm text-blue-800 space-y-1">
                <li>• 주문 확인 이메일이 발송됩니다.</li>
                <li>• 배송은 영업일 기준 2-3일 소요됩니다.</li>
                <li>• 주문 내역은 마이페이지에서 확인할 수 있습니다.</li>
                <li>• 문의사항이 있으시면 고객센터로 연락해주세요.</li>
              </ul>
            </div>
          </div>
        </div>

        {/* 액션 버튼 */}
        <div className="flex flex-col sm:flex-row gap-4">
          <Link href={`/orders/${orderInfo.orderId}`} className="flex-1">
            <Button variant="outline" fullWidth>
              주문 상세보기
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
          <p className="text-sm font-medium text-gray-700">
            고객센터: 1588-0000 (평일 09:00~18:00)
          </p>
        </div>
      </div>
    </div>
  );
}