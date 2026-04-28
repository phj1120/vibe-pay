package com.api.app.repository.rodb.order

import com.api.app.entity.OrderGoods
import com.api.app.entity.OrderGoodsId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderCompleteGoodsProjection {
    fun getGoodsNo(): String?
    fun getItemNo(): String?
    fun getGoodsName(): String?
    fun getItemName(): String?
    fun getSalePrice(): Long?
    fun getQuantity(): Long?
    fun getSubtotal(): Long?
}

interface OrderGoodsRepository : JpaRepository<OrderGoods, OrderGoodsId> {

    fun findByIdOrderNo(orderNo: String): List<OrderGoods>

    fun findByIdOrderNoAndIdGoodsNoAndIdItemNo(
        orderNo: String, goodsNo: String, itemNo: String
    ): OrderGoods?

    @Query(
        value = """
        SELECT OG.GOODS_NO AS goodsNo
             , OG.ITEM_NO AS itemNo
             , OG.GOODS_NAME AS goodsName
             , OG.ITEM_NAME AS itemName
             , OG.SALE_PRICE AS salePrice
             , OD.QUANTITY AS quantity
             , (OG.SALE_PRICE * OD.QUANTITY) AS subtotal
          FROM ORDER_GOODS OG
         INNER JOIN ORDER_DETAIL OD ON OG.ORDER_NO = OD.ORDER_NO
           AND OG.GOODS_NO = OD.GOODS_NO AND OG.ITEM_NO = OD.ITEM_NO
         WHERE OG.ORDER_NO = :orderNo
           AND OD.ORDER_TYPE_CODE = '001' AND OD.ORDER_PROCESS_SEQUENCE = 1
         ORDER BY OD.ORDER_SEQUENCE
        """,
        nativeQuery = true
    )
    fun selectOrderCompleteGoodsByOrderNo(orderNo: String): List<OrderCompleteGoodsProjection>
}
