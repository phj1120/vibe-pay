'use client';

import React from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import { Member } from '@/types/member';
import { getMember } from '@/lib/api/members';
import { getMemberOrders } from '@/lib/api/orders';
import { getMemberPayments } from '@/lib/api/payments';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Loading from '@/components/ui/Loading';
import { formatDate, formatPhoneNumber, formatCurrency } from '@/lib/formatters';
import { Order } from '@/types/order';
import { Payment } from '@/types/payment';
import { PageResponse } from '@/types/api';

// 회원 상세 페이지 컴포넌트
export default function MemberDetailPage() {
  const params = useParams();
  const memberId = Number(params.id);

  const [member, setMember] = React.useState<Member>();
  const [orders, setOrders] = React.useState<PageResponse<Order>>();
  const [payments, setPayments] = React.useState<PageResponse<Payment>>();
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string>();

  // 회원 정보 및 관련 데이터 조회
  const fetchMemberData = React.useCallback(async () => {
    try {
      setLoading(true);
      setError(undefined);

      // 병렬로 데이터 조회
      const [memberData, ordersData, paymentsData] = await Promise.all([
        getMember(memberId),
        getMemberOrders(memberId, { page: 0, size: 5 }),
        getMemberPayments(memberId, { page: 0, size: 5 }),
      ]);

      setMember(memberData);
      setOrders(ordersData);
      setPayments(paymentsData);
    } catch (err) {
      setError('회원 정보를 불러오는데 실패했습니다.');
      console.error('Failed to fetch member data:', err);
    } finally {
      setLoading(false);
    }
  }, [memberId]);

  // 컴포넌트 마운트 시 데이터 조회
  React.useEffect(() => {
    if (memberId) {
      fetchMemberData();
    }
  }, [memberId, fetchMemberData]);

  // 로딩 상태
  if (loading) {
    return <Loading.Page message="회원 정보를 불러오는 중..." />;
  }

  // 에러 상태
  if (error || !member) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            {error || '회원을 찾을 수 없습니다'}
          </h1>
          <Link href="/members">
            <Button>회원 목록으로 돌아가기</Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* 헤더 */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">{member.name}</h1>
            <p className="text-gray-600 mt-1">회원 ID: {member.memberId}</p>
          </div>
          <div className="flex gap-2">
            <Link href="/members">
              <Button variant="outline">목록으로</Button>
            </Link>
            <Button>정보 수정</Button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 회원 기본 정보 */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <Card.Header title="기본 정보" />
            <Card.Body>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="text-sm font-medium text-gray-500">이름</label>
                  <p className="text-lg text-gray-900 mt-1">{member.name}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">이메일</label>
                  <p className="text-lg text-gray-900 mt-1">{member.email}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">전화번호</label>
                  <p className="text-lg text-gray-900 mt-1">
                    {formatPhoneNumber(member.phoneNumber)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">가입일</label>
                  <p className="text-lg text-gray-900 mt-1">
                    {formatDate(member.createdAt)}
                  </p>
                </div>
                <div className="md:col-span-2">
                  <label className="text-sm font-medium text-gray-500">배송 주소</label>
                  <p className="text-lg text-gray-900 mt-1">{member.shippingAddress}</p>
                </div>
              </div>
            </Card.Body>
          </Card>

          {/* 최근 주문 내역 */}
          <Card>
            <Card.Header
              title="최근 주문 내역"
              action={
                <Link href={`/members/${memberId}/orders`}>
                  <Button variant="outline" size="sm">전체 보기</Button>
                </Link>
              }
            />
            <Card.Body>
              {orders && orders.content.length > 0 ? (
                <div className="space-y-4">
                  {orders.content.map((order) => (
                    <div key={order.orderId} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg">
                      <div>
                        <p className="font-medium text-gray-900">{order.orderId}</p>
                        <p className="text-sm text-gray-500">
                          {formatDate(order.orderDate)} • {order.status}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-lg font-medium text-gray-900">
                          {formatCurrency(order.totalAmount)}
                        </p>
                        <Link href={`/orders/${order.orderId}`}>
                          <Button variant="ghost" size="sm">상세보기</Button>
                        </Link>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center text-gray-500 py-8">
                  <p>주문 내역이 없습니다.</p>
                </div>
              )}
            </Card.Body>
          </Card>
        </div>

        {/* 사이드바 */}
        <div className="space-y-6">
          {/* 회원 통계 */}
          <Card>
            <Card.Header title="회원 통계" />
            <Card.Body>
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-gray-600">총 주문 수</span>
                  <span className="font-medium">
                    {orders?.totalElements || 0}건
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">총 결제 금액</span>
                  <span className="font-medium">
                    {formatCurrency(
                      payments?.content.reduce((sum, payment) => sum + payment.amount, 0) || 0
                    )}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">가입 기간</span>
                  <span className="font-medium">
                    {Math.floor(
                      (new Date().getTime() - new Date(member.createdAt).getTime()) /
                      (1000 * 60 * 60 * 24)
                    )}일
                  </span>
                </div>
              </div>
            </Card.Body>
          </Card>

          {/* 최근 결제 내역 */}
          <Card>
            <Card.Header title="최근 결제" />
            <Card.Body>
              {payments && payments.content.length > 0 ? (
                <div className="space-y-3">
                  {payments.content.slice(0, 3).map((payment) => (
                    <div key={payment.paymentId} className="flex justify-between items-center">
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          {payment.paymentMethod}
                        </p>
                        <p className="text-xs text-gray-500">
                          {formatDate(payment.paymentDate)}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-medium text-gray-900">
                          {formatCurrency(payment.amount)}
                        </p>
                        <p className="text-xs text-gray-500">{payment.status}</p>
                      </div>
                    </div>
                  ))}
                  <Link href={`/members/${memberId}/payments`}>
                    <Button variant="outline" fullWidth size="sm">
                      전체 결제 내역
                    </Button>
                  </Link>
                </div>
              ) : (
                <div className="text-center text-gray-500 py-4">
                  <p className="text-sm">결제 내역이 없습니다.</p>
                </div>
              )}
            </Card.Body>
          </Card>

          {/* 빠른 액션 */}
          <Card>
            <Card.Header title="빠른 액션" />
            <Card.Body>
              <div className="space-y-2">
                <Button variant="outline" fullWidth>
                  회원 정보 수정
                </Button>
                <Button variant="outline" fullWidth>
                  주문 내역 보기
                </Button>
                <Button variant="outline" fullWidth>
                  결제 내역 보기
                </Button>
                <Button variant="danger" fullWidth>
                  회원 탈퇴
                </Button>
              </div>
            </Card.Body>
          </Card>
        </div>
      </div>
    </div>
  );
}