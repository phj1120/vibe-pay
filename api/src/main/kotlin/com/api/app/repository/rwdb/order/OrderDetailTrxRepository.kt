package com.api.app.repository.rwdb.order

import com.api.app.entity.OrderDetail
import com.api.app.entity.OrderDetailId
import org.springframework.data.jpa.repository.JpaRepository

interface OrderDetailTrxRepository : JpaRepository<OrderDetail, OrderDetailId>
