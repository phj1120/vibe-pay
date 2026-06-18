package com.api.app.repository.rodb.basket

import com.api.app.vo.BasketVo

interface BasketBaseRepositoryCustom {
    fun selectBasketListByMemberNo(memberNo: String): List<BasketVo>
    fun selectBasketListByBasketNos(basketNos: List<String>): List<BasketVo>
}
