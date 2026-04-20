package com.vibepay.goods.repository

import com.vibepay.goods.entity.GoodsItem
import com.vibepay.goods.entity.GoodsItemId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface GoodsItemProjection {
    fun getGoodsNo(): String?
    fun getItemNo(): String?
    fun getItemName(): String?
    fun getItemPrice(): Long?
    fun getStock(): Long?
    fun getGoodsStatusCode(): String?
}

interface GoodsItemRepository : JpaRepository<GoodsItem, GoodsItemId> {

    @Query(
        value = """
        SELECT GOODS_NO AS goodsNo, ITEM_NO AS itemNo, ITEM_NAME AS itemName,
               ITEM_PRICE AS itemPrice, STOCK AS stock, GOODS_STATUS_CODE AS goodsStatusCode
          FROM GOODS_ITEM
         WHERE GOODS_NO = :goodsNo
         ORDER BY ITEM_NO
        """,
        nativeQuery = true
    )
    fun selectGoodsItemsByGoodsNo(goodsNo: String): List<GoodsItemProjection>

    fun findByIdGoodsNoAndIdItemNo(goodsNo: String, itemNo: String): GoodsItem?

    @Modifying
    @Query("DELETE FROM GoodsItem g WHERE g.id.goodsNo = :goodsNo")
    fun deleteByGoodsNo(goodsNo: String): Int
}
