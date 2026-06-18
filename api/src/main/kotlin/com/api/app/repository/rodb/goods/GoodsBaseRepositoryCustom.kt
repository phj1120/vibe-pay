package com.api.app.repository.rodb.goods

import com.api.app.vo.GoodsDetailVo
import com.api.app.vo.GoodsListVo

interface GoodsBaseRepositoryCustom {
    fun selectGoodsList(goodsStatusCode: String?, goodsName: String?, size: Int, offset: Long): List<GoodsListVo>
    fun countGoodsList(goodsStatusCode: String?, goodsName: String?): Long
    fun selectGoodsDetail(goodsNo: String): GoodsDetailVo?
}
