package com.api.app.service.basket

import com.api.app.dto.request.basket.BasketAddRequest
import com.api.app.dto.request.basket.BasketModifyRequest
import com.api.app.dto.response.basket.BasketResponse

interface BasketService {
    fun getBasketList(email: String): List<BasketResponse>
    fun addBasket(email: String, request: BasketAddRequest): String
    fun modifyBasket(email: String, basketNo: String, request: BasketModifyRequest)
    fun deleteBasket(email: String, basketNo: String)
    fun deleteBaskets(email: String, basketNos: List<String>)
    fun deleteAllBaskets(email: String)
}
