package com.api.app.client.dto

import java.time.LocalDateTime

data class MemberValidateResponse(
    val memberNo: String,
    val email: String,
    val memberStatusCode: String,
    val memberName: String
)

data class PointTransactionRequest(
    val amount: Long,
    val pointTransactionCode: String,
    val pointTransactionReasonCode: String,
    val pointTransactionReasonNo: String? = null
)

data class BasketResponse(
    val basketNo: String? = null,
    val memberNo: String? = null,
    val goodsNo: String? = null,
    val goodsName: String? = null,
    val goodsStatusCode: String? = null,
    val goodsMainImageUrl: String? = null,
    val salePrice: Long? = null,
    val itemNo: String? = null,
    val itemName: String? = null,
    val itemPrice: Long? = null,
    val itemStatusCode: String? = null,
    val stock: Long? = null,
    val quantity: Long? = null,
    val isOrder: Boolean? = null,
    val registDateTime: LocalDateTime? = null
)

data class GoodsValidateRequest(
    val items: List<GoodsValidateItem>
)

data class GoodsValidateItem(
    val goodsNo: String,
    val itemNo: String,
    val quantity: Long,
    val expectedSalePrice: Long
)

data class GoodsValidateResponse(
    val valid: Boolean,
    val items: List<GoodsItemSnapshot> = emptyList(),
    val errorMessage: String? = null
)

data class GoodsItemSnapshot(
    val goodsNo: String,
    val itemNo: String,
    val goodsName: String,
    val itemName: String,
    val salePrice: Long,
    val supplyPrice: Long,
    val stock: Long
)
