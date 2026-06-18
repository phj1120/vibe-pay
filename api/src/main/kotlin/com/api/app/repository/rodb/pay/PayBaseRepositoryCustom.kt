package com.api.app.repository.rodb.pay

import com.api.app.vo.OrderCompletePaymentVo

interface PayBaseRepositoryCustom {
    fun selectOrderCompletePaymentByOrderNo(orderNo: String): List<OrderCompletePaymentVo>
}
