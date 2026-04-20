package com.vibepay.core.service

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.vibepay.core.dto.request.basket.BasketAddRequest
import com.vibepay.core.dto.request.basket.BasketModifyRequest
import com.vibepay.core.dto.response.basket.BasketResponse
import com.vibepay.core.entity.BasketBase
import com.vibepay.core.repository.BasketProjection
import com.vibepay.core.repository.BasketRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BasketServiceImpl(
    private val basketRepository: BasketRepository,
    private val memberService: MemberService
) : BasketService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun getBasketList(email: String): List<BasketResponse> {
        val memberNo = getMemberNo(email)
        return basketRepository.selectBasketListByMemberNo(memberNo).map { it.toResponse() }
    }

    @Transactional
    override fun addBasket(email: String, request: BasketAddRequest): String {
        val memberNo = getMemberNo(email)
        val existing = basketRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(memberNo, request.goodsNo, request.itemNo)
        return if (existing != null) {
            existing.quantity += request.quantity
            basketRepository.save(existing)
            existing.basketNo
        } else {
            val basket = BasketBase().apply {
                this.memberNo = memberNo
                goodsNo = request.goodsNo
                itemNo = request.itemNo
                quantity = request.quantity
                isOrder = false
            }
            basketRepository.save(basket)
            basket.basketNo
        }
    }

    @Transactional
    override fun modifyBasket(email: String, basketNo: String, request: BasketModifyRequest) {
        val memberNo = getMemberNo(email)
        val basket = basketRepository.findById(basketNo).orElseThrow { ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다") }
        if (basket.memberNo != memberNo) throw ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 수정할 수 있습니다")
        request.goodsNo?.let { basket.goodsNo = it }
        request.itemNo?.let { basket.itemNo = it }
        request.quantity?.let { basket.quantity = it }
        basketRepository.save(basket)
    }

    @Transactional
    override fun deleteBasket(email: String, basketNo: String) {
        val memberNo = getMemberNo(email)
        val basket = basketRepository.findById(basketNo).orElseThrow { ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다") }
        if (basket.memberNo != memberNo) throw ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 삭제할 수 있습니다")
        basketRepository.deleteById(basketNo)
    }

    @Transactional
    override fun deleteBaskets(email: String, basketNos: List<String>) {
        if (basketNos.isEmpty()) throw ApiException(ApiError.INVALID_PARAMETER, "삭제할 장바구니를 선택해주세요")
        val memberNo = getMemberNo(email)
        basketNos.forEach { basketNo ->
            val basket = basketRepository.findById(basketNo).orElseThrow { ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다: $basketNo") }
            if (basket.memberNo != memberNo) throw ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 삭제할 수 있습니다")
        }
        basketRepository.deleteByBasketNoIn(basketNos)
    }

    @Transactional
    override fun deleteAllBaskets(email: String) {
        val memberNo = getMemberNo(email)
        basketRepository.deleteByMemberNo(memberNo)
    }

    @Transactional
    override fun markBasketAsOrdered(basketNo: String) {
        basketRepository.updateBasketIsOrder(basketNo, "system")
        log.info("장바구니 주문 처리 완료: basketNo={}", basketNo)
    }

    override fun getBasketsByNos(basketNos: List<String>): List<BasketResponse> =
        basketRepository.selectBasketListByBasketNos(basketNos).map { it.toResponse() }

    private fun getMemberNo(email: String): String =
        memberService.findByEmail(email)?.memberNo ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")

    private fun BasketProjection.toResponse() = BasketResponse(
        basketNo = getBasketNo(), memberNo = getMemberNo(), goodsNo = getGoodsNo(),
        goodsName = getGoodsName(), goodsStatusCode = getGoodsStatusCode(),
        goodsMainImageUrl = getGoodsMainImageUrl(), salePrice = getSalePrice(),
        itemNo = getItemNo(), itemName = getItemName(), itemPrice = getItemPrice(),
        itemStatusCode = getItemStatusCode(), stock = getStock(), quantity = getQuantity(),
        isOrder = getIsOrder(), registDateTime = getRegistDateTime()
    )
}
