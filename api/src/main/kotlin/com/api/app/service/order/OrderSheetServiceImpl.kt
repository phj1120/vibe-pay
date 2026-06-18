package com.api.app.service.order

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.response.basket.BasketResponse
import com.api.app.dto.response.order.OrderSheetResponse
import com.api.app.emum.PRD001
import com.api.app.repository.rodb.basket.BasketBaseRepository
import com.api.app.vo.BasketVo
import com.api.app.repository.rodb.member.MemberBaseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderSheetServiceImpl(
    private val basketBaseRepository: BasketBaseRepository,
    private val memberBaseRepository: MemberBaseRepository
) : OrderSheetService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun getOrderSheet(email: String, basketNos: List<String>): OrderSheetResponse {
        if (basketNos.isEmpty()) throw ApiException(ApiError.INVALID_PARAMETER, "장바구니 번호가 필요합니다")

        val member = memberBaseRepository.findByEmail(email)
            ?: throw ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다")

        val basketList = basketBaseRepository.selectBasketListByBasketNos(basketNos)

        if (basketList.isEmpty()) throw ApiException(ApiError.DATA_NOT_FOUND, "유효하지 않은 장바구니입니다")
        if (basketList.size != basketNos.size) throw ApiException(ApiError.DATA_NOT_FOUND, "일부 장바구니를 찾을 수 없습니다")

        val memberNo = member.memberNo
        basketList.forEach { basket ->
            if (basket.memberNo != memberNo) {
                throw ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 주문할 수 있습니다")
            }
            if (basket.isOrder == true) {
                throw ApiException(ApiError.INVALID_PARAMETER, "이미 주문된 상품입니다: ${basket.goodsName}")
            }
            if (!PRD001.ON_SALE.code.equals(basket.goodsStatusCode)
                || !PRD001.ON_SALE.code.equals(basket.itemStatusCode)) {
                throw ApiException(ApiError.INVALID_PARAMETER, "판매 중인 상품만 주문할 수 있습니다: ${basket.goodsName}")
            }
            val stock = basket.stock ?: 0L
            val quantity = basket.quantity ?: 0L
            if (stock < quantity) {
                throw ApiException(ApiError.INVALID_PARAMETER,
                    "재고가 부족합니다: ${basket.goodsName} (재고: $stock, 주문수량: $quantity)")
            }
        }

        val items = basketList.map { it.toResponse() }
        val totalProductAmount = items.sumOf { (it.salePrice ?: 0L) * (it.quantity ?: 0L) }
        val totalQuantity = items.sumOf { it.quantity ?: 0L }

        log.info("주문서 정보 조회 완료: email={}, items={}, totalAmount={}", email, items.size, totalProductAmount)

        return OrderSheetResponse(
            items = items,
            ordererName = member.memberName,
            ordererEmail = member.email,
            ordererPhone = member.phone,
            totalProductAmount = totalProductAmount,
            totalQuantity = totalQuantity
        )
    }

    private fun BasketVo.toResponse() = BasketResponse(
        basketNo = basketNo,
        memberNo = memberNo,
        goodsNo = goodsNo,
        goodsName = goodsName,
        goodsStatusCode = goodsStatusCode,
        goodsMainImageUrl = goodsMainImageUrl,
        salePrice = salePrice,
        itemNo = itemNo,
        itemName = itemName,
        itemPrice = itemPrice,
        itemStatusCode = itemStatusCode,
        stock = stock,
        quantity = quantity,
        isOrder = isOrder,
        registDateTime = registDateTime
    )
}
