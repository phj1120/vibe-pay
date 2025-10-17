'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useCart } from '@/context/CartContext';
import { createOrder, generateOrderNumber } from '@/lib/api/orders';
import { initiatePayment, confirmPaymentWithPG } from '@/lib/api/payments';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Loading from '@/components/ui/Loading';
import { formatCurrency } from '@/lib/formatters';
import { CreateOrderRequest } from '@/types/order';
import { PaymentMethod, PgCompany, PaymentInitiateRequest } from '@/types/payment';

// 주문 페이지 컴포넌트
export default function OrderPage() {
  const router = useRouter();
  const { state: cartState, clearCart } = useCart();

  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string>();
  const [orderNumber, setOrderNumber] = React.useState<string>();

  // 주문자 정보
  const [orderForm, setOrderForm] = React.useState({
    memberId: 1, // TODO: 실제 로그인된 사용자 ID
    name: '',
    email: '',
    phoneNumber: '',
    shippingAddress: '',
  });

  // 결제 정보
  const [paymentForm, setPaymentForm] = React.useState({
    paymentMethod: 'CREDIT_CARD' as PaymentMethod,
    pgCompany: 'INICIS' as PgCompany,
  });

  // 장바구니가 비어있으면 상품 페이지로 리다이렉트
  React.useEffect(() => {
    if (cartState.items.length === 0) {
      alert('장바구니가 비어있습니다. 상품을 선택해주세요.');
      router.push('/products');
    }
  }, [cartState.items.length, router]);

  // 주문 번호 미리 채번
  React.useEffect(() => {
    const fetchOrderNumber = async () => {
      try {
        const number = await generateOrderNumber();
        setOrderNumber(number);
        console.log('Order number generated:', number);
      } catch (err) {
        console.error('Failed to generate order number:', err);
        setError('주문 번호 생성에 실패했습니다.');
      }
    };

    if (cartState.items.length > 0 && !orderNumber) {
      fetchOrderNumber();
    }
  }, [cartState.items.length, orderNumber]);

  // postMessage 리스너 등록 (PG 결제창으로부터 결제 결과 수신)
  React.useEffect(() => {
    const handlePaymentResult = async (event: MessageEvent) => {
      // 출처 검증
      if (event.origin !== window.location.origin) {
        console.warn('Ignoring message from unknown origin:', event.origin);
        return;
      }

      // 메시지 타입 확인
      if (event.data.type === 'PAYMENT_RESULT') {
        console.log('Payment result received:', event.data.data);

        const { resultCode, authToken, authUrl, netCancelUrl, mid, orderId, amount } = event.data.data;

        // 결제 성공 여부 확인 (이니시스: resultCode === "0000")
        if (resultCode === '0000') {
          try {
            setLoading(true);
            setError(undefined);

            // 결제 승인 API 호출
            console.log('Confirming payment...');
            const confirmResult = await confirmPaymentWithPG({
              orderId: orderId || orderNumber!,
              authToken,
              authUrl,
              mid,
              netCancelUrl,
              amount: amount || cartState.totalAmount,
              memberId: orderForm.memberId,
              paymentMethod: paymentForm.paymentMethod,
              pgCompany: paymentForm.pgCompany,
            });

            console.log('Payment confirmed:', confirmResult);

            // 주문 생성
            const orderRequest: CreateOrderRequest = {
              memberId: orderForm.memberId,
              orderNumber: orderId || orderNumber!,
              orderItems: cartState.items.map(item => ({
                productId: item.product.productId,
                quantity: item.quantity,
              })),
              paymentMethods: [{
                paymentMethod: paymentForm.paymentMethod,
                amount: cartState.totalAmount,
              }],
            };

            console.log('Creating order...');
            const order = await createOrder(orderRequest);
            console.log('Order created:', order);

            // 성공 시 장바구니 비우고 완료 페이지로 이동
            clearCart();
            router.push(`/order/complete?orderId=${order.orderId || orderId}&paymentId=${confirmResult.paymentId}`);

          } catch (err) {
            console.error('Payment confirmation or order creation failed:', err);
            setError('결제 승인 또는 주문 생성 중 오류가 발생했습니다.');
            router.push('/order/failed');
          } finally {
            setLoading(false);
          }
        } else {
          // 결제 실패
          console.error('Payment failed:', event.data.data);
          alert(`결제 실패: ${event.data.data.resultMsg || '알 수 없는 오류'}`);
          setLoading(false);
        }
      }
    };

    window.addEventListener('message', handlePaymentResult);
    return () => window.removeEventListener('message', handlePaymentResult);
  }, [orderNumber, cartState, orderForm, paymentForm, clearCart, router]);

  // 폼 입력 처리
  const handleOrderFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setOrderForm(prev => ({ ...prev, [name]: value }));
  };

  const handlePaymentFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setPaymentForm(prev => ({ ...prev, [name]: value }));
  };

  // PG 결제창 팝업 열기
  const openPaymentPopup = (paymentUrl: string, parameters: Record<string, string>) => {
    try {
      // Form 엘리먼트 생성
      const form = document.createElement('form');
      form.method = 'POST';
      form.action = paymentUrl;
      form.target = 'payment_popup';
      form.style.display = 'none';

      // 파라미터 추가
      Object.entries(parameters).forEach(([key, value]) => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = key;
        input.value = value;
        form.appendChild(input);
      });

      document.body.appendChild(form);

      // 팝업 열기
      const popup = window.open(
        '',
        'payment_popup',
        'width=500,height=600,scrollbars=yes,resizable=yes'
      );

      if (!popup || popup.closed || typeof popup.closed === 'undefined') {
        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
        document.body.removeChild(form);
        setLoading(false);
        return;
      }

      // Form submit
      form.submit();
      document.body.removeChild(form);

      console.log('Payment popup opened successfully');
    } catch (err) {
      console.error('Failed to open payment popup:', err);
      alert('결제창 열기에 실패했습니다.');
      setLoading(false);
    }
  };

  // 주문 처리 (결제 초기화 -> 팝업 열기)
  const handleOrder = async (e: React.FormEvent) => {
    e.preventDefault();

    if (cartState.items.length === 0) {
      alert('주문할 상품이 없습니다.');
      return;
    }

    if (!orderNumber) {
      alert('주문 번호 생성 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }

    try {
      setLoading(true);
      setError(undefined);

      // 결제 초기화 (PG 결제창 파라미터 받기)
      console.log('Initiating payment...');
      const initRequest: PaymentInitiateRequest = {
        memberId: orderForm.memberId,
        amount: cartState.totalAmount,
        paymentMethod: paymentForm.paymentMethod,
        pgCompany: paymentForm.pgCompany,
        orderId: orderNumber,
        productName: cartState.items.map(item => item.product.name).join(', '),
        buyerName: orderForm.name,
        buyerPhone: orderForm.phoneNumber,
        buyerEmail: orderForm.email,
        returnUrl: `${window.location.origin}/payment/callback`,
        cancelUrl: `${window.location.origin}/payment/cancel`,
      };

      const initResponse = await initiatePayment(initRequest);
      console.log('Payment initiated:', initResponse);

      if (!initResponse.success) {
        throw new Error(initResponse.message || '결제 초기화 실패');
      }

      // PG 결제창 팝업 열기
      openPaymentPopup(initResponse.paymentUrl, initResponse.parameters);

    } catch (err) {
      console.error('Payment initiation failed:', err);
      setError('결제 초기화 중 오류가 발생했습니다. 다시 시도해주세요.');
      setLoading(false);
    }
  };

  // 장바구니가 비어있으면 렌더링하지 않음
  if (cartState.items.length === 0) {
    return <Loading.Page message="상품 페이지로 이동 중..." />;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">주문하기</h1>
        <p className="text-gray-600">주문 정보를 확인하고 결제를 진행해주세요.</p>
        {orderNumber && (
          <p className="text-sm text-gray-500 mt-2">주문번호: {orderNumber}</p>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p className="text-red-800">{error}</p>
        </div>
      )}

      <form onSubmit={handleOrder}>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 주문 정보 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 주문자 정보 */}
            <Card>
              <Card.Header title="주문자 정보" />
              <Card.Body>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <Input
                    label="이름"
                    name="name"
                    value={orderForm.name}
                    onChange={handleOrderFormChange}
                    required
                  />
                  <Input
                    label="이메일"
                    name="email"
                    type="email"
                    value={orderForm.email}
                    onChange={handleOrderFormChange}
                    required
                  />
                  <Input
                    label="전화번호"
                    name="phoneNumber"
                    type="tel"
                    value={orderForm.phoneNumber}
                    onChange={handleOrderFormChange}
                    required
                  />
                  <div className="md:col-span-2">
                    <Input
                      label="배송 주소"
                      name="shippingAddress"
                      value={orderForm.shippingAddress}
                      onChange={handleOrderFormChange}
                      required
                    />
                  </div>
                </div>
              </Card.Body>
            </Card>

            {/* 결제 정보 */}
            <Card>
              <Card.Header title="결제 정보" />
              <Card.Body>
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        결제 방법
                      </label>
                      <select
                        name="paymentMethod"
                        value={paymentForm.paymentMethod}
                        onChange={handlePaymentFormChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                      >
                        <option value="CREDIT_CARD">신용카드</option>
                        <option value="BANK_TRANSFER">계좌이체</option>
                        <option value="VIRTUAL_ACCOUNT">가상계좌</option>
                        <option value="POINT">포인트</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        PG사
                      </label>
                      <select
                        name="pgCompany"
                        value={paymentForm.pgCompany}
                        onChange={handlePaymentFormChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        required
                      >
                        <option value="INICIS">이니시스</option>
                        <option value="NICEPAY">나이스페이</option>
                        <option value="TOSS">토스페이먼츠</option>
                        <option value="KAKAO">카카오페이</option>
                      </select>
                    </div>
                  </div>

                  <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
                    <p className="text-sm text-blue-800">
                      💳 PG사 결제창에서 카드 정보를 입력하시게 됩니다.
                    </p>
                  </div>
                </div>
              </Card.Body>
            </Card>
          </div>

          {/* 주문 요약 */}
          <div>
            <Card>
              <Card.Header title="주문 요약" />
              <Card.Body>
                <div className="space-y-4">
                  {/* 상품 목록 */}
                  <div className="space-y-3">
                    {cartState.items.map((item) => (
                      <div key={item.product.productId} className="flex justify-between items-center">
                        <div className="flex-1">
                          <p className="font-medium text-gray-900 text-sm">
                            {item.product.name}
                          </p>
                          <p className="text-gray-500 text-sm">
                            {formatCurrency(item.product.price)} × {item.quantity}
                          </p>
                        </div>
                        <p className="font-medium text-gray-900">
                          {formatCurrency(item.product.price * item.quantity)}
                        </p>
                      </div>
                    ))}
                  </div>

                  <hr />

                  {/* 금액 요약 */}
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">상품 금액</span>
                      <span>{formatCurrency(cartState.totalAmount)}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">배송비</span>
                      <span>무료</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">할인</span>
                      <span>-{formatCurrency(0)}</span>
                    </div>
                    <hr />
                    <div className="flex justify-between text-lg font-bold">
                      <span>총 결제 금액</span>
                      <span className="text-blue-600">
                        {formatCurrency(cartState.totalAmount)}
                      </span>
                    </div>
                  </div>
                </div>
              </Card.Body>
              <Card.Footer>
                <Button
                  type="submit"
                  fullWidth
                  size="lg"
                  loading={loading}
                  disabled={loading || !orderNumber}
                >
                  {loading ? '결제 처리 중...' : `${formatCurrency(cartState.totalAmount)} 결제하기`}
                </Button>
              </Card.Footer>
            </Card>
          </div>
        </div>
      </form>
    </div>
  );
}
