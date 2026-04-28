package com.api.app.repository.rwdb.member

import com.api.app.entity.MemberBase
import org.springframework.data.jpa.repository.JpaRepository

interface MemberBaseTrxRepository : JpaRepository<MemberBase, String>
