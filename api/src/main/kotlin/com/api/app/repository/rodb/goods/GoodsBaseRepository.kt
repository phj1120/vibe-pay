package com.api.app.repository.rodb.goods

import com.api.app.entity.GoodsBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface GoodsListProjection {
    fun getGoodsNo(): String?
    fun getGoodsName(): String?
    fun getGoodsStatusCode(): String?
    fun getGoodsStatusName(): String?
    fun getGoodsMainImageUrl(): String?
    fun getSalePrice(): Long?
    fun getSupplyPrice(): Long?
}

interface GoodsDetailProjection {
    fun getGoodsNo(): String?
    fun getGoodsName(): String?
    fun getGoodsStatusCode(): String?
    fun getGoodsStatusName(): String?
    fun getGoodsMainImageUrl(): String?
    fun getSalePrice(): Long?
    fun getSupplyPrice(): Long?
    fun getRegistDateTime(): LocalDateTime?
    fun getModifyDateTime(): LocalDateTime?
}

interface GoodsBaseRepository : JpaRepository<GoodsBase, String> {

    @Query(
        value = """
        SELECT G.GOODS_NO AS goodsNo
             , G.GOODS_NAME AS goodsName
             , G.GOODS_STATUS_CODE AS goodsStatusCode
             , C.CODE_NAME AS goodsStatusName
             , G.GOODS_MAIN_IMAGE_URL AS goodsMainImageUrl
             , P.SALE_PRICE AS salePrice
             , P.SUPPLY_PRICE AS supplyPrice
          FROM GOODS_BASE G
         INNER JOIN GOODS_PRICE_HIST P ON G.GOODS_NO = P.GOODS_NO
           AND CURRENT_TIMESTAMP BETWEEN P.START_DATE_TIME AND P.END_DATE_TIME
          LEFT JOIN CODE_DETAIL C ON C.GROUP_CODE = 'PRD001' AND C.CODE = G.GOODS_STATUS_CODE
         WHERE (:goodsStatusCode IS NULL OR G.GOODS_STATUS_CODE = :goodsStatusCode)
           AND (:goodsName IS NULL OR G.GOODS_NAME LIKE '%' || :goodsName || '%')
         ORDER BY G.REGIST_DATE_TIME DESC
         LIMIT :size OFFSET :offset
        """,
        nativeQuery = true
    )
    fun selectGoodsList(
        goodsStatusCode: String?,
        goodsName: String?,
        size: Int,
        offset: Long
    ): List<GoodsListProjection>

    @Query(
        value = """
        SELECT COUNT(*) FROM GOODS_BASE G
         WHERE (:goodsStatusCode IS NULL OR G.GOODS_STATUS_CODE = :goodsStatusCode)
           AND (:goodsName IS NULL OR G.GOODS_NAME LIKE '%' || :goodsName || '%')
        """,
        nativeQuery = true
    )
    fun countGoodsList(goodsStatusCode: String?, goodsName: String?): Long

    @Query(
        value = """
        SELECT G.GOODS_NO AS goodsNo
             , G.GOODS_NAME AS goodsName
             , G.GOODS_STATUS_CODE AS goodsStatusCode
             , C.CODE_NAME AS goodsStatusName
             , G.GOODS_MAIN_IMAGE_URL AS goodsMainImageUrl
             , P.SALE_PRICE AS salePrice
             , P.SUPPLY_PRICE AS supplyPrice
             , G.REGIST_DATE_TIME AS registDateTime
             , G.MODIFY_DATE_TIME AS modifyDateTime
          FROM GOODS_BASE G
         INNER JOIN GOODS_PRICE_HIST P ON G.GOODS_NO = P.GOODS_NO
           AND CURRENT_TIMESTAMP BETWEEN P.START_DATE_TIME AND P.END_DATE_TIME
          LEFT JOIN CODE_DETAIL C ON C.GROUP_CODE = 'PRD001' AND C.CODE = G.GOODS_STATUS_CODE
         WHERE G.GOODS_NO = :goodsNo
        """,
        nativeQuery = true
    )
    fun selectGoodsDetail(goodsNo: String): GoodsDetailProjection?
}
