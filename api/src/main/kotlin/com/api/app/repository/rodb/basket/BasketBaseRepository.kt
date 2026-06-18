package com.api.app.repository.rodb.basket

import com.api.app.entity.BasketBase
import org.springframework.data.jpa.repository.JpaRepository

interface BasketBaseRepository : JpaRepository<BasketBase, String>, BasketBaseRepositoryCustom {

    fun findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(
        memberNo: String, goodsNo: String, itemNo: String
    ): BasketBase?
}
