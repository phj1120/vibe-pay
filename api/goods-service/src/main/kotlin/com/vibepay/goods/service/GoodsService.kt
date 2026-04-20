package com.vibepay.goods.service

import com.vibepay.goods.dto.internal.GoodsValidateRequest
import com.vibepay.goods.dto.internal.GoodsValidateResponse
import com.vibepay.goods.dto.request.GoodsModifyRequest
import com.vibepay.goods.dto.request.GoodsRegisterRequest
import com.vibepay.goods.dto.request.GoodsSearchRequest
import com.vibepay.goods.dto.response.GoodsDetailResponse
import com.vibepay.goods.dto.response.GoodsPageResponse

interface GoodsService {
    fun registerGoods(request: GoodsRegisterRequest): String
    fun modifyGoods(goodsNo: String, request: GoodsModifyRequest)
    fun getGoodsList(request: GoodsSearchRequest): GoodsPageResponse
    fun getGoodsDetail(goodsNo: String): GoodsDetailResponse
    fun deleteGoods(goodsNo: String)
    fun validateGoods(request: GoodsValidateRequest): GoodsValidateResponse
}
