package com.api.app.service.order

import com.api.app.common.exception.ApiException
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
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class OrderSheetServiceImplTest {

    @InjectMocks
    private lateinit var orderSheetService: OrderSheetServiceImpl

    @Mock
    private lateinit var basketBaseRepository: BasketBaseRepository

    @Mock
    private lateinit var memberBaseRepository: MemberBaseRepository

    private fun createMember(memberNo: String = "M001", email: String = "test@example.com") =
        MemberBase().apply {
            this.memberNo = memberNo
            this.email = email
            this.memberName = "홍길동"
            this.phone = "010-1234-5678"
        }

    private fun mockProjection(
        basketNo: String,
        memberNo: String = "M001",
        goodsStatusCode: String = "SALE",
        itemStatusCode: String = "SALE",
        isOrder: Boolean = false,
        salePrice: Long = 10000L,
        quantity: Long = 2L,
        stock: Long = 10L
    ): BasketProjection {
        val p = mock(BasketProjection::class.java)
        given(p.getBasketNo()).willReturn(basketNo)
        given(p.getMemberNo()).willReturn(memberNo)
        given(p.getGoodsNo()).willReturn("G001")
        given(p.getGoodsName()).willReturn("상품$basketNo")
        given(p.getGoodsStatusCode()).willReturn(goodsStatusCode)
        given(p.getGoodsMainImageUrl()).willReturn(null)
        given(p.getSalePrice()).willReturn(salePrice)
        given(p.getItemNo()).willReturn("001")
        given(p.getItemName()).willReturn("기본")
        given(p.getItemPrice()).willReturn(0L)
        given(p.getItemStatusCode()).willReturn(itemStatusCode)
        given(p.getStock()).willReturn(stock)
        given(p.getQuantity()).willReturn(quantity)
        given(p.getIsOrder()).willReturn(isOrder)
        given(p.getRegistDateTime()).willReturn(null)
        return p
    }

    @Test
    @DisplayName("주문서 정보 조회 성공")
    fun getOrderSheet_Success() {
        val email = "test@example.com"
        val memberNo = "M001"
        val basketNos = listOf("B001", "B002")

        val basket1 = mockProjection("B001", salePrice = 10000L, quantity = 2L)
        val basket2 = mockProjection("B002", salePrice = 15000L, quantity = 1L)

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.selectBasketListByBasketNos(basketNos)).willReturn(listOf(basket1, basket2))

        val response = orderSheetService.getOrderSheet(email, basketNos)

        assertThat(response.ordererName).isEqualTo("홍길동")
        assertThat(response.ordererEmail).isEqualTo(email)
        assertThat(response.ordererPhone).isEqualTo("010-1234-5678")
        assertThat(response.items).hasSize(2)
        assertThat(response.totalProductAmount).isEqualTo(35000L) // (10000*2) + (15000*1)
        assertThat(response.totalQuantity).isEqualTo(3L)
    }

    @Test
    @DisplayName("장바구니 번호가 없으면 예외 발생")
    fun getOrderSheet_EmptyBasketNos() {
        assertThatThrownBy { orderSheetService.getOrderSheet("test@example.com", emptyList()) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("장바구니 번호가 필요합니다")
    }

    @Test
    @DisplayName("회원 정보가 없으면 예외 발생")
    fun getOrderSheet_MemberNotFound() {
        given(memberBaseRepository.findByEmail("test@example.com")).willReturn(null)

        assertThatThrownBy { orderSheetService.getOrderSheet("test@example.com", listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("회원 정보를 찾을 수 없습니다")
    }

    @Test
    @DisplayName("장바구니가 존재하지 않으면 예외 발생")
    fun getOrderSheet_BasketNotFound() {
        val email = "test@example.com"
        val basketNos = listOf("B001")

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember())
        given(basketBaseRepository.selectBasketListByBasketNos(basketNos)).willReturn(emptyList())

        assertThatThrownBy { orderSheetService.getOrderSheet(email, basketNos) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("유효하지 않은 장바구니입니다")
    }

    @Test
    @DisplayName("다른 회원의 장바구니면 예외 발생")
    fun getOrderSheet_ForbiddenBasket() {
        val email = "test@example.com"
        val basketNos = listOf("B001")

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember("M001", email))
        given(basketBaseRepository.selectBasketListByBasketNos(basketNos))
            .willReturn(listOf(mockProjection("B001", memberNo = "M002")))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, basketNos) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("본인의 장바구니만 주문할 수 있습니다")
    }

    @Test
    @DisplayName("이미 주문된 장바구니면 예외 발생")
    fun getOrderSheet_AlreadyOrdered() {
        val email = "test@example.com"
        val basketNos = listOf("B001")

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember())
        given(basketBaseRepository.selectBasketListByBasketNos(basketNos))
            .willReturn(listOf(mockProjection("B001", isOrder = true)))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, basketNos) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("이미 주문된 상품입니다")
    }

    @Test
    @DisplayName("상품이 판매중이 아니면 예외 발생")
    fun getOrderSheet_GoodsNotOnSale() {
        val email = "test@example.com"
        val basketNos = listOf("B001")

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember())
        given(basketBaseRepository.selectBasketListByBasketNos(basketNos))
            .willReturn(listOf(mockProjection("B001", goodsStatusCode = "SOLD_OUT")))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, basketNos) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("판매 중인 상품만 주문할 수 있습니다")
    }

    @Test
    @DisplayName("재고가 부족하면 예외 발생")
    fun getOrderSheet_InsufficientStock() {
        val email = "test@example.com"
        val basketNos = listOf("B001")

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember())
        given(basketBaseRepository.selectBasketListByBasketNos(basketNos))
            .willReturn(listOf(mockProjection("B001", quantity = 10L, stock = 5L)))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, basketNos) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("재고가 부족합니다")
    }
}
