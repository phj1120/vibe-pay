package com.api.app.dto.response.order

data class CancelableOrderResponse(
    val orderNo: String? = null,
    val cancelableItems: List<CancelableOrderItem> = emptyList(),
    val refundInfo: RefundInfo? = null
) {
    data class CancelableOrderItem(
        val orderSequence: Long? = null,
        val orderProcessSequence: Long? = null,
        val goodsNo: String? = null,
        val itemNo: String? = null,
        val goodsName: String? = null,
        val itemName: String? = null,
        val salePrice: Long? = null,
        val quantity: Long? = null,
        val subtotal: Long? = null
    )

    data class RefundInfo(
        val totalRefundAmount: Long? = null,
        val refundDetails: List<RefundDetail> = emptyList()
    )

    data class RefundDetail(
        val payWayCode: String? = null,
        val payWayName: String? = null,
        val refundAmount: Long? = null,
        val pgTypeCode: String? = null,
        val pgTypeName: String? = null
    )
}
