package com.api.app.repository.rwdb.goods

import com.api.app.entity.GoodsItem
import com.api.app.entity.GoodsItemId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface GoodsItemTrxRepository : JpaRepository<GoodsItem, GoodsItemId> {

    @Modifying
    @Query("DELETE FROM GoodsItem g WHERE g.id.goodsNo = :goodsNo")
    fun deleteByGoodsNo(goodsNo: String): Int
}
