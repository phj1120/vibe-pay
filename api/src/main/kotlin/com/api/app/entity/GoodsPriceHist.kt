package com.api.app.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Embeddable
data class GoodsPriceHistId(
    @Column(name = "GOODS_NO", length = 15) val goodsNo: String = "",
    @Column(name = "START_DATE_TIME") val startDateTime: LocalDateTime = LocalDateTime.now()
) : Serializable

@Entity
@Table(name = "GOODS_PRICE_HIST")
class GoodsPriceHist : SystemEntity() {

    @EmbeddedId
    var id: GoodsPriceHistId = GoodsPriceHistId()

    @Column(name = "END_DATE_TIME")
    var endDateTime: LocalDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59)

    @Column(name = "SALE_PRICE", nullable = false, columnDefinition = "numeric")
    var salePrice: Long = 0

    @Column(name = "SUPPLY_PRICE", nullable = false, columnDefinition = "numeric")
    var supplyPrice: Long = 0

    // Convenience accessors
    val goodsNo: String get() = id.goodsNo
    val startDateTime: LocalDateTime get() = id.startDateTime
}
