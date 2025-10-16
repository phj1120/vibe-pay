'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { useCart } from '@/context/CartContext';
import { createOrder } from '@/lib/api/orders';
import { processPayment } from '@/lib/api/payments';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Loading from '@/components/ui/Loading';
import { formatCurrency } from '@/lib/formatters';
import { CreateOrderRequest } from '@/types/order';
import { PaymentRequest, PaymentMethod, PgCompany } from '@/types/payment';

// 주문 페이지 컴포넌트
export default function OrderPage() {
  const router = useRouter();
  const { state: cartState, clearCart } = useCart();

  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState<string>();

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
    paymentMethod: 'CARD' as PaymentMethod,
    pgCompany: 'INICIS' as PgCompany,
    cardNumber: '',
    expiryDate: '',
    cvv: '',
  });

  // 장바구니가 비어있으면 상품 페이지로 리다이렉트
  React.useEffect(() => {
    if (cartState.items.length === 0) {
      alert('장바구니가 비어있습니다. 상품을 선택해주세요.');
      router.push('/products');
    }
  }, [cartState.items.length, router]);

  // 폼 입력 처리
  const handleOrderFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setOrderForm(prev => ({ ...prev, [name]: value }));
  };

  const handlePaymentFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setPaymentForm(prev => ({ ...prev, [name]: value }));
  };

  // 주문 처리
  const handleOrder = async (e: React.FormEvent) => {
    e.preventDefault();

    if (cartState.items.length === 0) {
      alert('주문할 상품이 없습니다.');
      return;
    }

    try {
      setLoading(true);
      setError(undefined);

      // 주문 요청 데이터 생성
      const orderRequest: CreateOrderRequest = {
        memberId: orderForm.memberId,
        orderItems: cartState.items.map(item => ({
          productId: item.product.productId,
          quantity: item.quantity,
        })),
        paymentMethods: [{
          paymentMethod: paymentForm.paymentMethod,
          amount: cartState.totalAmount,
        }],
      };

      // 주문 생성
      const order = await createOrder(orderRequest);
      console.log('Order created:', order);

      // 결제 요청 데이터 생성
      const paymentRequest: PaymentRequest = {
        orderId: order.orderId,
        paymentMethod: paymentForm.paymentMethod,
        amount: cartState.totalAmount,
        pgCompany: paymentForm.pgCompany,
      };

      // 결제 처리
      const paymentResult = await processPayment(paymentRequest);
      console.log('Payment processed:', paymentResult);

      // 성공 시 장바구니 비우고 완료 페이지로 이동
      clearCart();
      router.push(`/order/complete?orderId=${order.orderId}&paymentId=${paymentResult.paymentId}`);

    } catch (err) {
      console.error('Order/Payment failed:', err);
      setError('주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
      // 실패 시 실패 페이지로 이동
      router.push('/order/failed');
    } finally {
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
                        <option value="CARD">신용카드</option>
                        <option value="BANK_TRANSFER">계좌이체</option>
                        <option value="VIRTUAL_ACCOUNT">가상계좌</option>
                        <option value="POINTS">포인트</option>
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

                  {paymentForm.paymentMethod === 'CARD' && (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div className="md:col-span-2">
                        <Input
                          label="카드번호"
                          name="cardNumber"
                          placeholder="1234-5678-9012-3456"
                          value={paymentForm.cardNumber}
                          onChange={handlePaymentFormChange}
                          required
                        />
                      </div>
                      <Input
                        label="유효기간"
                        name="expiryDate"
                        placeholder="MM/YY"
                        value={paymentForm.expiryDate}
                        onChange={handlePaymentFormChange}
                        required
                      />
                      <Input
                        label="CVV"
                        name="cvv"
                        placeholder="123"
                        value={paymentForm.cvv}
                        onChange={handlePaymentFormChange}
                        required
                      />
                    </div>
                  )}
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
                  disabled={loading}
                >
                  {loading ? '주문 처리 중...' : `${formatCurrency(cartState.totalAmount)} 결제하기`}
                </Button>
              </Card.Footer>
            </Card>
          </div>
        </div>
      </form>
    </div>
  );
}