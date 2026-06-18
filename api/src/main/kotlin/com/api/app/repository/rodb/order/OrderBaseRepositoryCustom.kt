package com.api.app.repository.rodb.order

import com.api.app.vo.CancelableOrderItemVo
import com.api.app.vo.OrderCompleteHeaderVo
import com.api.app.vo.OrderListFlatVo
import com.api.app.vo.RefundDetailVo

interface OrderBaseRepositoryCustom {
    fun selectOrderCompleteByOrderNo(orderNo: String, memberNo: String): OrderCompleteHeaderVo?
    fun selectOrderListByMemberNo(memberNo: String): List<OrderListFlatVo>
    fun selectCancelableOrdersByOrderNo(orderNo: String, memberNo: String): List<CancelableOrderItemVo>
    fun selectRefundDetailsByOrderNo(orderNo: String): List<RefundDetailVo>
}
