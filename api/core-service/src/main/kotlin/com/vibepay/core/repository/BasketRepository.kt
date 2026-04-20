package com.vibepay.core.repository

import com.vibepay.core.entity.BasketBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface BasketProjection {
    fun getBasketNo(): String?
    fun getMemberNo(): String?
    fun getGoodsNo(): String?
    fun getGoodsName(): String?
    fun getGoodsStatusCode(): String?
    fun getGoodsMainImageUrl(): String?
    fun getSalePrice(): Long?
    fun getItemNo(): String?
    fun getItemName(): String?
    fun getItemPrice(): Long?
    fun getItemStatusCode(): String?
    fun getStock(): Long?
    fun getQuantity(): Long?
    fun getIsOrder(): Boolean?
    fun getRegistDateTime(): LocalDateTime?
}

interface BasketRepository : JpaRepository<BasketBase, String> {

    fun findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(memberNo: String, goodsNo: String, itemNo: String): BasketBase?

    @Query(
        value = """
        SELECT B.BASKET_NO AS basketNo, B.MEMBER_NO AS memberNo, B.GOODS_NO AS goodsNo,
               G.GOODS_NAME AS goodsName, G.GOODS_STATUS_CODE AS goodsStatusCode,
               G.GOODS_MAIN_IMAGE_URL AS goodsMainImageUrl,
               (COALESCE(P.SALE_PRICE, 0) + I.ITEM_PRICE) AS salePrice,
               B.ITEM_NO AS itemNo, I.ITEM_NAME AS itemName, I.ITEM_PRICE AS itemPrice,
               I.GOODS_STATUS_CODE AS itemStatusCode, I.STOCK AS stock,
               B.QUANTITY AS quantity, B.IS_ORDER AS isOrder, B.REGIST_DATE_TIME AS registDateTime
          FROM BASKET_BASE B
         INNER JOIN GOODS_BASE G ON B.GOODS_NO = G.GOODS_NO
         INNER JOIN GOODS_ITEM I ON B.GOODS_NO = I.GOODS_NO AND B.ITEM_NO = I.ITEM_NO
          LEFT JOIN GOODS_PRICE_HIST P ON B.GOODS_NO = P.GOODS_NO
           AND CURRENT_TIMESTAMP BETWEEN P.START_DATE_TIME AND P.END_DATE_TIME
         WHERE B.MEMBER_NO = :memberNo AND B.IS_ORDER = FALSE
         ORDER BY B.REGIST_DATE_TIME DESC
        """,
        nativeQuery = true
    )
    fun selectBasketListByMemberNo(memberNo: String): List<BasketProjection>

    @Query(
        value = """
        SELECT B.BASKET_NO AS basketNo, B.MEMBER_NO AS memberNo, B.GOODS_NO AS goodsNo,
               G.GOODS_NAME AS goodsName, G.GOODS_STATUS_CODE AS goodsStatusCode,
               G.GOODS_MAIN_IMAGE_URL AS goodsMainImageUrl,
               (COALESCE(P.SALE_PRICE, 0) + I.ITEM_PRICE) AS salePrice,
               B.ITEM_NO AS itemNo, I.ITEM_NAME AS itemName, I.ITEM_PRICE AS itemPrice,
               I.GOODS_STATUS_CODE AS itemStatusCode, I.STOCK AS stock,
               B.QUANTITY AS quantity, B.IS_ORDER AS isOrder, B.REGIST_DATE_TIME AS registDateTime
          FROM BASKET_BASE B
         INNER JOIN GOODS_BASE G ON B.GOODS_NO = G.GOODS_NO
         INNER JOIN GOODS_ITEM I ON B.GOODS_NO = I.GOODS_NO AND B.ITEM_NO = I.ITEM_NO
          LEFT JOIN GOODS_PRICE_HIST P ON B.GOODS_NO = P.GOODS_NO
           AND CURRENT_TIMESTAMP BETWEEN P.START_DATE_TIME AND P.END_DATE_TIME
         WHERE B.BASKET_NO IN :basketNos
         ORDER BY B.REGIST_DATE_TIME DESC
        """,
        nativeQuery = true
    )
    fun selectBasketListByBasketNos(basketNos: List<String>): List<BasketProjection>

    @Modifying
    @Query("DELETE FROM BasketBase b WHERE b.basketNo IN :basketNos")
    fun deleteByBasketNoIn(basketNos: List<String>): Int

    @Modifying
    @Query("DELETE FROM BasketBase b WHERE b.memberNo = :memberNo")
    fun deleteByMemberNo(memberNo: String): Int

    @Modifying
    @Query(
        value = "UPDATE BASKET_BASE SET IS_ORDER = TRUE, MODIFY_ID = :modifyId, MODIFY_DATE_TIME = NOW() WHERE BASKET_NO = :basketNo",
        nativeQuery = true
    )
    fun updateBasketIsOrder(basketNo: String, modifyId: String): Int
}
