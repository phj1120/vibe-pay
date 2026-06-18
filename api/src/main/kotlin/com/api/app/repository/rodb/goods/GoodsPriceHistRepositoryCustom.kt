package com.api.app.repository.rodb.goods

import com.api.app.entity.GoodsPriceHist

interface GoodsPriceHistRepositoryCustom {
    fun selectCurrentPrice(goodsNo: String): GoodsPriceHist?
}
