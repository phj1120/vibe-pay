package com.api.app.service.goods

import com.api.app.dto.request.goods.GoodsModifyRequest
import com.api.app.dto.request.goods.GoodsRegisterRequest
import com.api.app.dto.request.goods.GoodsSearchRequest
import com.api.app.dto.response.goods.GoodsDetailResponse
import com.api.app.dto.response.goods.GoodsPageResponse

interface GoodsService {
    fun registerGoods(request: GoodsRegisterRequest): String
    fun modifyGoods(goodsNo: String, request: GoodsModifyRequest)
    fun getGoodsList(request: GoodsSearchRequest): GoodsPageResponse
    fun getGoodsDetail(goodsNo: String): GoodsDetailResponse
    fun deleteGoods(goodsNo: String)
}
