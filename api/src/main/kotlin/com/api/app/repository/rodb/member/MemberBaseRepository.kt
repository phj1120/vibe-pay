package com.api.app.repository.rodb.member

import com.api.app.entity.MemberBase
import org.springframework.data.jpa.repository.JpaRepository

interface MemberBaseRepository : JpaRepository<MemberBase, String> {
    fun findByEmail(email: String): MemberBase?
    fun findByMemberNo(memberNo: String): MemberBase?
}
