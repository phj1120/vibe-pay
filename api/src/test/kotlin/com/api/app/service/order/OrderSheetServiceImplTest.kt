package com.api.app.service.order

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.emum.PRD001
import com.api.app.entity.MemberBase
import com.api.app.repository.rodb.basket.BasketBaseRepository
import com.api.app.repository.rodb.basket.BasketProjection
import com.api.app.repository.rodb.member.MemberBaseRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderSheetServiceImplTest {

    @InjectMocks
    private lateinit var orderSheetService: OrderSheetServiceImpl

    @Mock
    private lateinit var basketBaseRepository: BasketBaseRepository

    @Mock
    private lateinit var memberBaseRepository: MemberBaseRepository

    @Test
    @DisplayName("주문서 조회 성공")
    fun getOrderSheetSuccess() {
        val email = "test@example.com"
        val basket1 = createProjection(basketNo = "B001", salePrice = 10000L, quantity = 2L)
        val basket2 = createProjection(basketNo = "B002", salePrice = 15000L, quantity = 1L)
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001", "B002"))).willReturn(listOf(basket1, basket2))

        val response = orderSheetService.getOrderSheet(email, listOf("B001", "B002"))

        assertThat(response.items).hasSize(2)
        assertThat(response.ordererName).isEqualTo("홍길동")
        assertThat(response.totalProductAmount).isEqualTo(35000L)
        assertThat(response.totalQuantity).isEqualTo(3L)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 장바구니 번호 없음")
    fun getOrderSheetFailWhenBasketNosEmpty() {
        assertThatThrownBy { orderSheetService.getOrderSheet("test@example.com", emptyList()) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 회원 없음")
    fun getOrderSheetFailWhenMemberMissing() {
        given(memberBaseRepository.findByEmail("test@example.com")).willReturn(null)

        assertThatThrownBy { orderSheetService.getOrderSheet("test@example.com", listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 장바구니 없음")
    fun getOrderSheetFailWhenBasketMissing() {
        val email = "test@example.com"
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001"))).willReturn(emptyList())

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 일부 장바구니 누락")
    fun getOrderSheetFailWhenBasketCountMismatch() {
        val email = "test@example.com"
        val basket = createProjection(basketNo = "B001")
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001", "B002"))).willReturn(listOf(basket))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001", "B002")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 다른 회원 장바구니")
    fun getOrderSheetFailWhenForbiddenBasket() {
        val email = "test@example.com"
        val basket = createProjection(basketNo = "B001", memberNo = "M999")
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo = "M001", email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001"))).willReturn(listOf(basket))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.FORBIDDEN)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 이미 주문됨")
    fun getOrderSheetFailWhenAlreadyOrdered() {
        val email = "test@example.com"
        val basket = createProjection(basketNo = "B001", isOrder = true)
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001"))).willReturn(listOf(basket))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 상품 판매 상태 아님")
    fun getOrderSheetFailWhenGoodsNotOnSale() {
        val email = "test@example.com"
        val basket = createProjection(basketNo = "B001", goodsStatusCode = PRD001.SOLD_OUT.code)
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001"))).willReturn(listOf(basket))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 단품 판매 상태 아님")
    fun getOrderSheetFailWhenItemNotOnSale() {
        val email = "test@example.com"
        val basket = createProjection(basketNo = "B001", itemStatusCode = PRD001.DISCONTINUED.code)
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001"))).willReturn(listOf(basket))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("주문서 조회 실패 - 재고 부족")
    fun getOrderSheetFailWhenStockInsufficient() {
        val email = "test@example.com"
        val basket = createProjection(basketNo = "B001", quantity = 10L, stock = 5L)
        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(email = email))
        given(basketBaseRepository.selectBasketListByBasketNos(listOf("B001"))).willReturn(listOf(basket))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)
    }

    private fun createMember(
        memberNo: String = "M001",
        email: String = "test@example.com"
    ) = MemberBase().apply {
        this.memberNo = memberNo
        this.memberName = "홍길동"
        this.phone = "010-1234-5678"
        this.email = email
    }

    private fun createProjection(
        basketNo: String,
        memberNo: String = "M001",
        goodsStatusCode: String = PRD001.ON_SALE.code,
        itemStatusCode: String = PRD001.ON_SALE.code,
        isOrder: Boolean = false,
        salePrice: Long = 10000L,
        quantity: Long = 2L,
        stock: Long = 10L
    ): BasketProjection {
        val projection = mock(BasketProjection::class.java)
        doReturn(basketNo).`when`(projection).getBasketNo()
        doReturn(memberNo).`when`(projection).getMemberNo()
        doReturn("G001").`when`(projection).getGoodsNo()
        doReturn("상품$basketNo").`when`(projection).getGoodsName()
        doReturn(goodsStatusCode).`when`(projection).getGoodsStatusCode()
        doReturn("https://cdn.example.com/goods.jpg").`when`(projection).getGoodsMainImageUrl()
        doReturn(salePrice).`when`(projection).getSalePrice()
        doReturn("001").`when`(projection).getItemNo()
        doReturn("기본").`when`(projection).getItemName()
        doReturn(0L).`when`(projection).getItemPrice()
        doReturn(itemStatusCode).`when`(projection).getItemStatusCode()
        doReturn(stock).`when`(projection).getStock()
        doReturn(quantity).`when`(projection).getQuantity()
        doReturn(isOrder).`when`(projection).getIsOrder()
        doReturn(LocalDateTime.of(2026, 4, 28, 0, 0)).`when`(projection).getRegistDateTime()
        return projection
    }
}
