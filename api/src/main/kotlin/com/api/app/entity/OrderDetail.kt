package com.api.app.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Embeddable
data class OrderDetailId(
    @Column(name = "ORDER_NO", length = 15) val orderNo: String = "",
    @Column(name = "ORDER_SEQUENCE", columnDefinition = "numeric") val orderSequence: Long = 0,
    @Column(name = "ORDER_PROCESS_SEQUENCE", columnDefinition = "numeric") val orderProcessSequence: Long = 0
) : Serializable

@Entity
@Table(name = "ORDER_DETAIL")
class OrderDetail : SystemEntity() {

    @EmbeddedId
    var id: OrderDetailId = OrderDetailId()

    @Column(name = "UPPER_ORDER_PROCESS_SEQUENCE", columnDefinition = "numeric")
    var upperOrderProcessSequence: Long? = null

    @Column(name = "CLAIM_NO", length = 15)
    var claimNo: String? = null

    @Column(name = "GOODS_NO", nullable = false, length = 15)
    var goodsNo: String = ""

    @Column(name = "ITEM_NO", nullable = false, length = 3)
    var itemNo: String = ""

    @Column(name = "QUANTITY", nullable = false, columnDefinition = "numeric")
    var quantity: Long = 0

    @Column(name = "ORDER_STATUS_CODE", nullable = false, length = 3)
    var orderStatusCode: String = ""

    @Column(name = "DELIVERY_TYPE_CODE", nullable = false, length = 3)
    var deliveryTypeCode: String = ""

    @Column(name = "ORDER_TYPE_CODE", nullable = false, length = 3)
    var orderTypeCode: String = ""

    @Column(name = "ORDER_ACCEPT_DTM")
    var orderAcceptDtm: LocalDateTime? = null

    @Column(name = "ORDER_FINISH_DTM")
    var orderFinishDtm: LocalDateTime? = null

    // Convenience accessors
    val orderNo: String get() = id.orderNo
    val orderSequence: Long get() = id.orderSequence
    val orderProcessSequence: Long get() = id.orderProcessSequence
}
