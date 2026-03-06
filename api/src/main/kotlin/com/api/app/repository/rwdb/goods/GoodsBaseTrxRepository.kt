package com.api.app.repository.rwdb.goods

import com.api.app.entity.GoodsBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GoodsBaseTrxRepository : JpaRepository<GoodsBase, String> {

    @Query(value = "SELECT 'G' || LPAD(NEXTVAL('SEQ_GOODS_NO')::TEXT, 14, '0')", nativeQuery = true)
    fun generateGoodsNo(): String
}
