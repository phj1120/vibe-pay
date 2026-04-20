package com.vibepay.core.repository

import com.vibepay.core.entity.MemberBase
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<MemberBase, String> {
    fun findByEmail(email: String): MemberBase?
    fun findByMemberNo(memberNo: String): MemberBase?
}
