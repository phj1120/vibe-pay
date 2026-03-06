package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "BASKET_BASE")
class BasketBase : SystemEntity() {

    @Id
    @Column(name = "BASKET_NO", length = 15, nullable = false)
    var basketNo: String = ""

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""

    @Column(name = "GOODS_NO", nullable = false, length = 15)
    var goodsNo: String = ""

    @Column(name = "ITEM_NO", nullable = false, length = 3)
    var itemNo: String = ""

    @Column(name = "QUANTITY", nullable = false, columnDefinition = "numeric")
    var quantity: Long = 0

    @Column(name = "IS_ORDER", nullable = false)
    var isOrder: Boolean = false
}
