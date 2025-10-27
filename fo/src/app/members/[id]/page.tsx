'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'

interface Member {
  memberId: number
  name: string
  email: string | null
  phoneNumber: string | null
  shippingAddress: string | null
  createdAt: string
}

export default function MemberDetailPage() {
  const params = useParams()
  const router = useRouter()
  const [member, setMember] = useState<Member | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchMember = async () => {
      setLoading(true)
      try {
        const response = await fetch(
          `http://localhost:8080/api/members/${params.id}`
        )
        if (response.ok) {
          const data = await response.json()
          setMember(data)
        } else if (response.status === 404) {
          alert('회원을 찾을 수 없습니다')
          router.push('/members')
        } else {
          alert('회원 정보 조회 실패')
        }
      } catch (error) {
        console.error(error)
        alert('회원 정보 조회 중 오류 발생')
      } finally {
        setLoading(false)
      }
    }

    if (params.id) {
      fetchMember()
    }
  }, [params.id, router])

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">로딩 중...</div>
      </div>
    )
  }

  if (!member) {
    return null
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-8 flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link
            href="/members"
            className="text-blue-600 hover:text-blue-800 font-semibold"
          >
            ← 목록으로
          </Link>
          <h1 className="text-3xl font-bold">{member.name}</h1>
        </div>
        <Link
          href={`/members/${member.memberId}/edit`}
          className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-lg font-semibold"
        >
          수정
        </Link>
      </div>

      {/* Member Information Card */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-bold mb-4">회원 정보</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">
              회원 ID
            </label>
            <p className="text-gray-900">{member.memberId}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">
              이름
            </label>
            <p className="text-gray-900">{member.name}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">
              이메일
            </label>
            <p className="text-gray-900">{member.email ?? '-'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">
              전화번호
            </label>
            <p className="text-gray-900">{member.phoneNumber ?? '-'}</p>
          </div>
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-500 mb-1">
              배송 주소
            </label>
            <p className="text-gray-900">{member.shippingAddress ?? '-'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">
              가입일
            </label>
            <p className="text-gray-900">
              {new Date(member.createdAt).toLocaleString('ko-KR')}
            </p>
          </div>
        </div>
      </div>

      {/* Reward Points Section (Placeholder) */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-bold mb-4">적립금</h2>
        <div className="bg-gray-50 rounded-lg p-4 text-center">
          <p className="text-gray-500">
            적립금 정보는 Phase 2-3에서 구현 예정입니다
          </p>
        </div>
      </div>

      {/* Order History Section (Placeholder) */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-bold mb-4">주문 내역</h2>
        <div className="bg-gray-50 rounded-lg p-4 text-center">
          <p className="text-gray-500">
            주문 내역은 Phase 2-5에서 구현 예정입니다
          </p>
        </div>
      </div>
    </div>
  )
}
