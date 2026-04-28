package com.api.app.service.basket

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.basket.BasketAddRequest
import com.api.app.dto.request.basket.BasketModifyRequest
import com.api.app.dto.response.basket.BasketResponse
import com.api.app.entity.BasketBase
import com.api.app.repository.rodb.basket.BasketBaseRepository
import com.api.app.repository.rodb.basket.BasketProjection
import com.api.app.repository.rodb.member.MemberBaseRepository
import com.api.app.repository.rwdb.basket.BasketBaseTrxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BasketServiceImpl(
    private val basketBaseRepository: BasketBaseRepository,
    private val basketBaseTrxRepository: BasketBaseTrxRepository,
    private val memberBaseRepository: MemberBaseRepository
) : BasketService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun getBasketList(email: String): List<BasketResponse> {
        val memberNo = getMemberNoByEmail(email)
        return basketBaseRepository.selectBasketListByMemberNo(memberNo).map { it.toResponse() }
    }

    @Transactional
    override fun addBasket(email: String, request: BasketAddRequest): String {
        log.debug("장바구니 추가 시작: email={}, goodsNo={}, itemNo={}", email, request.goodsNo, request.itemNo)

        val memberNo = getMemberNoByEmail(email)

        val existing = basketBaseRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(
            memberNo, request.goodsNo, request.itemNo
        )

        return if (existing != null) {
            existing.quantity += request.quantity
            basketBaseTrxRepository.save(existing)
            log.info("장바구니 수량 증가 완료: basketNo={}", existing.basketNo)
            existing.basketNo
        } else {
            val basket = BasketBase().apply {
                this.memberNo = memberNo
                this.goodsNo = request.goodsNo
                this.itemNo = request.itemNo
                this.quantity = request.quantity
                this.isOrder = false
            }
            basketBaseTrxRepository.save(basket)
            log.info("장바구니 추가 완료: basketNo={}", basket.basketNo)
            basket.basketNo
        }
    }

    @Transactional
    override fun modifyBasket(email: String, basketNo: String, request: BasketModifyRequest) {
        val memberNo = getMemberNoByEmail(email)

        val basket = basketBaseRepository.findById(basketNo).orElseThrow {
            ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다")
        }

        if (basket.memberNo != memberNo) {
            throw ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 수정할 수 있습니다")
        }

        request.goodsNo?.let { basket.goodsNo = it }
        request.itemNo?.let { basket.itemNo = it }
        request.quantity?.let { basket.quantity = it }

        basketBaseTrxRepository.save(basket)
        log.info("장바구니 수정 완료: basketNo={}", basketNo)
    }

    @Transactional
    override fun deleteBasket(email: String, basketNo: String) {
        val memberNo = getMemberNoByEmail(email)

        val basket = basketBaseRepository.findById(basketNo).orElseThrow {
            ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다")
        }

        if (basket.memberNo != memberNo) {
            throw ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 삭제할 수 있습니다")
        }

        basketBaseTrxRepository.deleteById(basketNo)
        log.info("장바구니 삭제 완료: basketNo={}", basketNo)
    }

    @Transactional
    override fun deleteBaskets(email: String, basketNos: List<String>) {
        if (basketNos.isEmpty()) throw ApiException(ApiError.INVALID_PARAMETER, "삭제할 장바구니를 선택해주세요")

        val memberNo = getMemberNoByEmail(email)

        basketNos.forEach { basketNo ->
            val basket = basketBaseRepository.findById(basketNo).orElseThrow {
                ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다: $basketNo")
            }
            if (basket.memberNo != memberNo) {
                throw ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 삭제할 수 있습니다")
            }
        }

        basketBaseTrxRepository.deleteByBasketNoIn(basketNos)
        log.info("장바구니 여러 개 삭제 완료: email={}, count={}", email, basketNos.size)
    }

    @Transactional
    override fun deleteAllBaskets(email: String) {
        val memberNo = getMemberNoByEmail(email)
        basketBaseTrxRepository.deleteByMemberNo(memberNo)
        log.info("장바구니 전체 삭제 완료: email={}", email)
    }

    private fun getMemberNoByEmail(email: String): String =
        memberBaseRepository.findByEmail(email)?.memberNo
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")

    private fun BasketProjection.toResponse() = BasketResponse(
        basketNo = getBasketNo(),
        memberNo = getMemberNo(),
        goodsNo = getGoodsNo(),
        goodsName = getGoodsName(),
        goodsStatusCode = getGoodsStatusCode(),
        goodsMainImageUrl = getGoodsMainImageUrl(),
        salePrice = getSalePrice(),
        itemNo = getItemNo(),
        itemName = getItemName(),
        itemPrice = getItemPrice(),
        itemStatusCode = getItemStatusCode(),
        stock = getStock(),
        quantity = getQuantity(),
        isOrder = getIsOrder(),
        registDateTime = getRegistDateTime()
    )
}
