package com.api.app.entity

import jakarta.persistence.*
import java.io.Serializable

@Embeddable
data class OrderGoodsId(
    @Column(name = "ORDER_NO", length = 15) val orderNo: String = "",
    @Column(name = "GOODS_NO", length = 15) val goodsNo: String = "",
    @Column(name = "ITEM_NO", length = 3) val itemNo: String = ""
) : Serializable

@Entity
@Table(name = "ORDER_GOODS")
class OrderGoods : SystemEntity() {

    @EmbeddedId
    var id: OrderGoodsId = OrderGoodsId()

    @Column(name = "SALE_PRICE", nullable = false, columnDefinition = "numeric")
    var salePrice: Long = 0

    @Column(name = "SUPPLY_PRICE", nullable = false, columnDefinition = "numeric")
    var supplyPrice: Long = 0

    @Column(name = "GOODS_NAME", nullable = false, length = 200)
    var goodsName: String = ""

    @Column(name = "ITEM_NAME", nullable = false, length = 100)
    var itemName: String = ""

    // Convenience accessors
    val orderNo: String get() = id.orderNo
    val goodsNo: String get() = id.goodsNo
    val itemNo: String get() = id.itemNo
}
