package com.api.app.repository.rodb.order

import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderDetailId
import org.springframework.data.jpa.repository.JpaRepository

interface OrderDetailRepository : JpaRepository<OrderDetail, OrderDetailId> {

    fun findByIdOrderNo(orderNo: String): List<OrderDetail>

    fun findByIdOrderNoAndIdOrderSequence(orderNo: String, orderSequence: Long): List<OrderDetail>

    fun findByIdOrderNoAndIdOrderSequenceAndIdOrderProcessSequence(
        orderNo: String, orderSequence: Long, orderProcessSequence: Long
    ): OrderDetail?
}
