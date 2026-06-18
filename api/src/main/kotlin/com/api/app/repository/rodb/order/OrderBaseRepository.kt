package com.api.app.repository.rodb.order

import com.api.app.entity.OrderBase
import org.springframework.data.jpa.repository.JpaRepository

interface OrderBaseRepository : JpaRepository<OrderBase, String>, OrderBaseRepositoryCustom {

    fun findByMemberNo(memberNo: String): List<OrderBase>
}
