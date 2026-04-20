package com.vibepay.core.service

import com.vibepay.core.dto.request.basket.BasketAddRequest
import com.vibepay.core.dto.request.basket.BasketModifyRequest
import com.vibepay.core.dto.response.basket.BasketResponse

interface BasketService {
    fun getBasketList(email: String): List<BasketResponse>
    fun addBasket(email: String, request: BasketAddRequest): String
    fun modifyBasket(email: String, basketNo: String, request: BasketModifyRequest)
    fun deleteBasket(email: String, basketNo: String)
    fun deleteBaskets(email: String, basketNos: List<String>)
    fun deleteAllBaskets(email: String)
    fun markBasketAsOrdered(basketNo: String)
    fun getBasketsByNos(basketNos: List<String>): List<BasketResponse>
}
