package com.api.app.repository.rodb.goods

import com.api.app.entity.GoodsItem
import com.api.app.entity.GoodsItemId
import org.springframework.data.jpa.repository.JpaRepository

interface GoodsItemRepository : JpaRepository<GoodsItem, GoodsItemId> {

    fun findByIdGoodsNoOrderByIdItemNoAsc(goodsNo: String): List<GoodsItem>

    fun findByIdGoodsNoAndIdItemNo(goodsNo: String, itemNo: String): GoodsItem?
}
