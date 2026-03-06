package com.api.app.repository.rwdb.goods

import com.api.app.entity.GoodsPriceHist
import com.api.app.entity.GoodsPriceHistId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface GoodsPriceHistTrxRepository : JpaRepository<GoodsPriceHist, GoodsPriceHistId> {

    @Modifying
    @Query(
        value = """
        UPDATE GOODS_PRICE_HIST
           SET END_DATE_TIME = CURRENT_TIMESTAMP
             , MODIFY_DATE_TIME = CURRENT_TIMESTAMP
         WHERE GOODS_NO = :goodsNo
           AND CURRENT_TIMESTAMP BETWEEN START_DATE_TIME AND END_DATE_TIME
        """,
        nativeQuery = true
    )
    fun updatePreviousPriceEndDateTime(goodsNo: String): Int
}
