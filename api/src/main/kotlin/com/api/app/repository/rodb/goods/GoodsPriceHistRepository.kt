package com.api.app.repository.rodb.goods

import com.api.app.entity.GoodsPriceHist
import com.api.app.entity.GoodsPriceHistId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GoodsPriceHistRepository : JpaRepository<GoodsPriceHist, GoodsPriceHistId> {

    @Query(
        value = """
        SELECT GOODS_NO, START_DATE_TIME, END_DATE_TIME, SALE_PRICE, SUPPLY_PRICE,
               REGIST_ID, REGIST_DATE_TIME, MODIFY_ID, MODIFY_DATE_TIME
          FROM GOODS_PRICE_HIST
         WHERE GOODS_NO = :goodsNo
           AND CURRENT_TIMESTAMP BETWEEN START_DATE_TIME AND END_DATE_TIME
         LIMIT 1
        """,
        nativeQuery = true
    )
    fun selectCurrentPrice(goodsNo: String): GoodsPriceHist?
}
