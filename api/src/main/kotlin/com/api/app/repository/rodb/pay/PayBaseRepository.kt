package com.api.app.repository.rodb.pay

import com.api.app.entity.PayBase
import org.springframework.data.jpa.repository.JpaRepository

interface PayBaseRepository : JpaRepository<PayBase, String>, PayBaseRepositoryCustom {

    fun findByOrderNo(orderNo: String): List<PayBase>
    fun findByMemberNo(memberNo: String): List<PayBase>
    fun findByApproveNo(approveNo: String): PayBase?
}
