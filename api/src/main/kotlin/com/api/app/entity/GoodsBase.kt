package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "GOODS_BASE")
class GoodsBase : SystemEntity() {

    @Id
    @Column(name = "GOODS_NO", length = 15, nullable = false)
    var goodsNo: String = ""

    @Column(name = "GOODS_NAME", nullable = false, length = 200)
    var goodsName: String = ""

    @Column(name = "GOODS_STATUS_CODE", nullable = false, length = 3)
    var goodsStatusCode: String = ""

    @Column(name = "GOODS_MAIN_IMAGE_URL", length = 500)
    var goodsMainImageUrl: String? = null
}
