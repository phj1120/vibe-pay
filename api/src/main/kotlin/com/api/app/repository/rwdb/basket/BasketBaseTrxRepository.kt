package com.api.app.repository.rwdb.basket

import com.api.app.entity.BasketBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface BasketBaseTrxRepository : JpaRepository<BasketBase, String> {

    @Modifying
    @Query("DELETE FROM BasketBase b WHERE b.basketNo IN :basketNos")
    fun deleteByBasketNoIn(basketNos: List<String>): Int

    @Modifying
    @Query("DELETE FROM BasketBase b WHERE b.memberNo = :memberNo")
    fun deleteByMemberNo(memberNo: String): Int

    @Modifying
    @Query(
        value = """
        UPDATE BASKET_BASE
           SET IS_ORDER = TRUE
             , MODIFY_ID = :modifyId
             , MODIFY_DATE_TIME = NOW()
         WHERE BASKET_NO = :basketNo
        """,
        nativeQuery = true
    )
    fun updateBasketIsOrder(basketNo: String, modifyId: String): Int
}
