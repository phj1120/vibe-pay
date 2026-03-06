package com.api.app.repository.rwdb.member

import com.api.app.entity.MemberBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberBaseTrxRepository : JpaRepository<MemberBase, String> {

    @Query(value = "SELECT LPAD(NEXTVAL('SEQ_MEMBER_NO')::TEXT, 15, '0')", nativeQuery = true)
    fun generateMemberNo(): String
}
