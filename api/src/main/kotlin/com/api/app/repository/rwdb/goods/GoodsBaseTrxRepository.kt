package com.api.app.repository.rwdb.goods

import com.api.app.entity.GoodsBase
import org.springframework.data.jpa.repository.JpaRepository

interface GoodsBaseTrxRepository : JpaRepository<GoodsBase, String>
