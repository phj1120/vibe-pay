package com.vibepay.goods.entity

import com.api.app.entity.SystemEntity
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable

@Embeddable
data class GoodsItemId(
    @Column(name = "GOODS_NO", length = 15) val goodsNo: String = "",
    @Column(name = "ITEM_NO", length = 3) val itemNo: String = ""
) : Serializable

@Entity
@Table(name = "GOODS_ITEM")
class GoodsItem : SystemEntity() {

    @EmbeddedId
    var id: GoodsItemId = GoodsItemId()

    @Column(name = "ITEM_NAME", nullable = false, length = 100)
    var itemName: String = ""

    @Column(name = "ITEM_PRICE", nullable = false, columnDefinition = "numeric")
    var itemPrice: Long = 0

    @Column(name = "STOCK", nullable = false, columnDefinition = "numeric")
    var stock: Long = 0

    @Column(name = "GOODS_STATUS_CODE", nullable = false, length = 3)
    var goodsStatusCode: String = ""

    val goodsNo: String get() = id.goodsNo
    val itemNo: String get() = id.itemNo
}
