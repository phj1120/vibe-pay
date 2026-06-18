package com.api.app.repository.rodb.order

import com.api.app.entity.OrderGoods
import com.api.app.entity.OrderGoodsId
import org.springframework.data.jpa.repository.JpaRepository

interface OrderGoodsRepository : JpaRepository<OrderGoods, OrderGoodsId>, OrderGoodsRepositoryCustom {

    fun findByIdOrderNo(orderNo: String): List<OrderGoods>

    fun findByIdOrderNoAndIdGoodsNoAndIdItemNo(
        orderNo: String, goodsNo: String, itemNo: String
    ): OrderGoods?
}
