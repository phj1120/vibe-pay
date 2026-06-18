package com.api.app.repository.rodb.goods

import com.api.app.entity.GoodsBase
import org.springframework.data.jpa.repository.JpaRepository

interface GoodsBaseRepository : JpaRepository<GoodsBase, String>, GoodsBaseRepositoryCustom
