'use client';

import React, { useEffect } from 'react';
import Loading from '@/components/ui/Loading';

/**
 * 결제 콜백 페이지
 *
 * PG사 결제창에서 결제 완료 후 리다이렉트되는 페이지입니다.
 * 이 페이지는 팝업 창 내에서 실행되며, 결제 결과를 부모 창으로 전달합니다.
 *
 * Flow:
 * 1. PG사 결제창에서 결제 완료
 * 2. 이 페이지로 리다이렉트 (returnUrl)
 * 3. URL 파라미터에서 PG사 응답 데이터 추출
 * 4. window.opener.postMessage()로 부모 창에 전달
 * 5. 팝업 창 자동 닫기
 */
export default function PaymentCallbackPage() {
  useEffect(() => {
    const handlePaymentCallback = () => {
      try {
        // URL 파라미터에서 PG사 응답 데이터 추출
        const params = new URLSearchParams(window.location.search);

        // 이니시스 응답 파라미터
        const resultCode = params.get('resultCode');
        const authToken = params.get('authToken');
        const authUrl = params.get('authUrl');
        const netCancelUrl = params.get('netCancelUrl');
        const mid = params.get('mid');
        const oid = params.get('oid'); // 주문번호
        const price = params.get('price');
        const resultMsg = params.get('resultMsg');

        console.log('Payment callback received:', {
          resultCode,
          oid,
          price,
          resultMsg
        });

        // 결제 결과 데이터 구성
        const paymentResult = {
          resultCode,
          authToken,
          authUrl,
          netCancelUrl,
          mid,
          orderId: oid,
          amount: price ? parseInt(price, 10) : 0,
          resultMsg
        };

        // 부모 창 확인
        if (window.opener && !window.opener.closed) {
          console.log('Sending payment result to parent window...');

          // 부모 창으로 결제 결과 전달
          window.opener.postMessage(
            {
              type: 'PAYMENT_RESULT',
              data: paymentResult
            },
            window.location.origin // 동일 출처만 허용
          );

          // 잠시 후 팝업 창 닫기 (부모 창이 메시지를 받을 시간 확보)
          setTimeout(() => {
            console.log('Closing payment popup...');
            window.close();
          }, 1000);
        } else {
          console.error('Parent window not found or closed');
          alert('부모 창을 찾을 수 없습니다. 이 창을 닫고 다시 시도해주세요.');
        }
      } catch (error) {
        console.error('Payment callback error:', error);
        alert('결제 결과 처리 중 오류가 발생했습니다.');
      }
    };

    // 컴포넌트 마운트 시 즉시 실행
    handlePaymentCallback();
  }, []);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <Loading.Spinner size="lg" />
        <p className="mt-4 text-lg font-medium text-gray-700">
          결제 결과를 처리하고 있습니다...
        </p>
        <p className="mt-2 text-sm text-gray-500">
          잠시만 기다려주세요.
        </p>
        <p className="mt-4 text-xs text-gray-400">
          이 창은 자동으로 닫힙니다.
        </p>
      </div>
    </div>
  );
}
