package com.api.app.repository.rwdb.order

import com.api.app.entity.OrderGoods
import com.api.app.entity.OrderGoodsId
import org.springframework.data.jpa.repository.JpaRepository

interface OrderGoodsTrxRepository : JpaRepository<OrderGoods, OrderGoodsId>
