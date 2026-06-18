package com.api.app.repository.rodb.order

import com.api.app.vo.OrderCompleteGoodsVo

interface OrderGoodsRepositoryCustom {
    fun selectOrderCompleteGoodsByOrderNo(orderNo: String): List<OrderCompleteGoodsVo>
}
