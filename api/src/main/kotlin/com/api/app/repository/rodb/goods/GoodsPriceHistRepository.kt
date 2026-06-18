package com.api.app.repository.rodb.goods

import com.api.app.entity.GoodsPriceHist
import com.api.app.entity.GoodsPriceHistId
import org.springframework.data.jpa.repository.JpaRepository

interface GoodsPriceHistRepository : JpaRepository<GoodsPriceHist, GoodsPriceHistId>, GoodsPriceHistRepositoryCustom
