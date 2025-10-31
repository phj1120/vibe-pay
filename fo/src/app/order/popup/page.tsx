'use client';

import { useEffect, useRef, useState } from 'react';
import { getOrderCookie, deleteOrderCookie } from '@/lib/order-cookie';
import { callInicisPay, callNicePay } from '@/lib/pg-utils';
import type { OrderCookieData } from '@/types/order.types';

/**
 * 결제 팝업 페이지
 *
 * 흐름:
 * 1. 쿠키에서 주문 정보 읽기
 * 2. PG사별 form 생성
 * 3. 쿠키 삭제
 * 4. PG 결제창 호출
 */
export default function PaymentPopupPage() {
  const [error, setError] = useState<string | null>(null);
  const [orderData, setOrderData] = useState<OrderCookieData | null>(null);
  const formRef = useRef<HTMLFormElement>(null);
  const isInitialized = useRef(false);

  useEffect(() => {
    if (isInitialized.current) {
      return;
    }
    isInitialized.current = true;

    const cookieData = getOrderCookie();

    if (!cookieData) {
      setError('주문 정보를 찾을 수 없습니다. 다시 시도해주세요.');
      setTimeout(() => {
        window.close();
      }, 2000);
      return;
    }

    setOrderData(cookieData);
    deleteOrderCookie();
  }, []);

  useEffect(() => {
    if (!orderData || !formRef.current) {
      return;
    }

    const { paymentInitiate } = orderData;

    if (!paymentInitiate.formData || Object.keys(paymentInitiate.formData).length === 0) {
      setError('PG 결제 정보가 없습니다.');
      return;
    }

    try {
      if (paymentInitiate.pgType === 'INICIS') {
        const script = document.createElement('script');
        script.src = 'https://stdpay.inicis.com/stdjs/INIStdPay.js';
        script.onload = () => {
          setTimeout(() => {
            try {
              callInicisPay('inicisForm');
            } catch (err) {
              setError(err instanceof Error ? err.message : 'PG 호출 실패');
            }
          }, 100);
        };
        script.onerror = () => {
          setError('결제 모듈 로딩에 실패했습니다.');
        };
        document.body.appendChild(script);
      } else if (paymentInitiate.pgType === 'NICE') {
        // 나이스페이 콜백 함수 정의 (전역)
        (window as any).nicepaySubmit = function() {
          console.log('nicepaySubmit callback called');
          // form이 이미 인증 응답 데이터로 채워져 있음
          // form을 서버로 submit
          if (formRef.current) {
            console.log('Submitting Nice payment form to server');
            formRef.current.submit();
          }
        };

        const script = document.createElement('script');
        script.src = 'https://web.nicepay.co.kr/v3/webstd/js/nicepay-3.0.js';
        script.onload = () => {
          if (formRef.current) {
            callNicePay(formRef.current);
          }
        };
        script.onerror = () => {
          setError('결제 모듈 로딩에 실패했습니다.');
        };
        document.body.appendChild(script);
      }
    } catch (error) {
      setError(error instanceof Error ? error.message : '결제 요청에 실패했습니다.');
    }
  }, [orderData]);

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen p-4">
        <div className="text-center">
          <p className="text-red-500 mb-4">{error}</p>
          <button
            onClick={() => window.close()}
            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
          >
            닫기
          </button>
        </div>
      </div>
    );
  }

  if (!orderData) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p>로딩 중...</p>
      </div>
    );
  }

  const { paymentInitiate } = orderData;

  return (
    <div className="flex items-center justify-center min-h-screen p-4">
      <div className="text-center">
        <p className="mb-4">결제창을 불러오는 중입니다...</p>
        <p className="text-sm text-gray-600">잠시만 기다려주세요.</p>

        {paymentInitiate.pgType === 'INICIS' && (
          <form id="inicisForm" name="inicisForm" method="post" ref={formRef}>
            {Object.entries(paymentInitiate.formData).map(([key, value]) => (
              <input key={key} type="hidden" name={key} value={value} />
            ))}
          </form>
        )}

        {paymentInitiate.pgType === 'NICE' && (
          <form
            id="nicePayForm"
            name="nicePayForm"
            method="post"
            acceptCharset="euc-kr"
            action="/api/order/payment/return"
            ref={formRef}
          >
            {Object.entries(paymentInitiate.formData).map(([key, value]) => (
              <input key={key} type="hidden" name={key} value={value} />
            ))}
          </form>
        )}
      </div>
    </div>
  );
}
