'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'

interface Member {
  memberId: number
  name: string
  email: string | null
  phoneNumber: string | null
  shippingAddress: string | null
  createdAt: string
}

interface MemberFormData {
  name: string
  email: string
  phoneNumber: string
  shippingAddress: string
}

export default function MembersPage() {
  const [members, setMembers] = useState<Member[]>([])
  const [loading, setLoading] = useState(false)
  const [showDialog, setShowDialog] = useState(false)
  const [showDeleteDialog, setShowDeleteDialog] = useState(false)
  const [isEditMode, setIsEditMode] = useState(false)
  const [editedMember, setEditedMember] = useState<MemberFormData>({
    name: '',
    email: '',
    phoneNumber: '',
    shippingAddress: ''
  })
  const [currentEditId, setCurrentEditId] = useState<number | null>(null)
  const [memberToDelete, setMemberToDelete] = useState<Member | null>(null)
  const [searchTerm, setSearchTerm] = useState('')

  const fetchMembers = async () => {
    setLoading(true)
    try {
      const response = await fetch('http://localhost:8080/api/members')
      if (response.ok) {
        const data = await response.json()
        setMembers(data)
      } else {
        alert('회원 목록 조회 실패')
      }
    } catch (error) {
      console.error(error)
      alert('회원 목록 조회 중 오류 발생')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchMembers()
  }, [])

  const openCreateDialog = () => {
    setIsEditMode(false)
    setEditedMember({
      name: '',
      email: '',
      phoneNumber: '',
      shippingAddress: ''
    })
    setCurrentEditId(null)
    setShowDialog(true)
  }

  const openEditDialog = (member: Member) => {
    setIsEditMode(true)
    setEditedMember({
      name: member.name,
      email: member.email ?? '',
      phoneNumber: member.phoneNumber ?? '',
      shippingAddress: member.shippingAddress ?? ''
    })
    setCurrentEditId(member.memberId)
    setShowDialog(true)
  }

  const saveMember = async () => {
    if (!editedMember.name.trim()) {
      alert('이름은 필수 항목입니다')
      return
    }

    const url = isEditMode
      ? `http://localhost:8080/api/members/${currentEditId}`
      : 'http://localhost:8080/api/members'

    const method = isEditMode ? 'PUT' : 'POST'

    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(editedMember)
      })

      if (response.ok) {
        setShowDialog(false)
        fetchMembers()
        alert(isEditMode ? '회원 정보가 수정되었습니다' : '회원이 생성되었습니다')
      } else {
        alert('저장 실패')
      }
    } catch (error) {
      console.error(error)
      alert('저장 중 오류 발생')
    }
  }

  const openDeleteDialog = (member: Member) => {
    setMemberToDelete(member)
    setShowDeleteDialog(true)
  }

  const deleteMember = async () => {
    if (!memberToDelete) return

    try {
      const response = await fetch(
        `http://localhost:8080/api/members/${memberToDelete.memberId}`,
        { method: 'DELETE' }
      )

      if (response.ok) {
        setShowDeleteDialog(false)
        fetchMembers()
        alert('회원이 삭제되었습니다')
      } else if (response.status === 409) {
        alert('주문 내역이 있는 회원은 삭제할 수 없습니다')
      } else {
        alert('삭제 실패')
      }
    } catch (error) {
      console.error(error)
      alert('삭제 중 오류 발생')
    }
  }

  const filteredMembers = members.filter(
    (member) =>
      member.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (member.email?.toLowerCase() ?? '').includes(searchTerm.toLowerCase())
  )

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold mb-2">회원 관리</h1>
        <p className="text-gray-600">회원 목록 및 정보 관리</p>
      </div>

      <div className="mb-6 flex justify-between items-center">
        <input
          type="text"
          placeholder="이름 또는 이메일로 검색"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="px-4 py-2 border rounded-lg w-1/3"
        />
        <button
          onClick={openCreateDialog}
          className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-lg font-semibold"
        >
          + 회원 추가
        </button>
      </div>

      {loading ? (
        <div className="text-center py-8">로딩 중...</div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  이름
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  이메일
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  전화번호
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  생성일
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  액션
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredMembers.map((member) => (
                <tr key={member.memberId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {member.memberId}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    <Link
                      href={`/members/${member.memberId}`}
                      className="text-blue-600 hover:text-blue-800"
                    >
                      {member.name}
                    </Link>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {member.email ?? '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {member.phoneNumber ?? '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(member.createdAt).toLocaleDateString('ko-KR')}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button
                      onClick={() => openEditDialog(member)}
                      className="text-indigo-600 hover:text-indigo-900 mr-4"
                    >
                      수정
                    </button>
                    <button
                      onClick={() => openDeleteDialog(member)}
                      className="text-red-600 hover:text-red-900"
                    >
                      삭제
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create/Edit Dialog */}
      {showDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">
              {isEditMode ? '회원 수정' : '회원 추가'}
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  이름 *
                </label>
                <input
                  type="text"
                  value={editedMember.name}
                  onChange={(e) =>
                    setEditedMember({ ...editedMember, name: e.target.value })
                  }
                  className="w-full px-3 py-2 border rounded-lg"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  이메일
                </label>
                <input
                  type="email"
                  value={editedMember.email}
                  onChange={(e) =>
                    setEditedMember({ ...editedMember, email: e.target.value })
                  }
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  전화번호
                </label>
                <input
                  type="tel"
                  value={editedMember.phoneNumber}
                  onChange={(e) =>
                    setEditedMember({
                      ...editedMember,
                      phoneNumber: e.target.value
                    })
                  }
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  배송 주소
                </label>
                <input
                  type="text"
                  value={editedMember.shippingAddress}
                  onChange={(e) =>
                    setEditedMember({
                      ...editedMember,
                      shippingAddress: e.target.value
                    })
                  }
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-6">
              <button
                onClick={() => setShowDialog(false)}
                className="px-4 py-2 border rounded-lg hover:bg-gray-100"
              >
                취소
              </button>
              <button
                onClick={saveMember}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
              >
                저장
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Dialog */}
      {showDeleteDialog && memberToDelete && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-sm">
            <h2 className="text-xl font-bold mb-4">회원 삭제</h2>
            <p className="mb-6">
              <strong>{memberToDelete.name}</strong> 회원을 삭제하시겠습니까?
            </p>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowDeleteDialog(false)}
                className="px-4 py-2 border rounded-lg hover:bg-gray-100"
              >
                취소
              </button>
              <button
                onClick={deleteMember}
                className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600"
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
