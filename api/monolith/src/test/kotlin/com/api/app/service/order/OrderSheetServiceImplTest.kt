package com.api.app.service.order

import com.api.app.client.CoreFeignClient
import com.api.app.client.dto.BasketResponse
import com.api.app.client.dto.MemberValidateResponse
import com.api.app.common.exception.ApiException
import com.api.app.common.response.ApiResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class OrderSheetServiceImplTest {

    @InjectMocks
    private lateinit var orderSheetService: OrderSheetServiceImpl

    @Mock
    private lateinit var coreFeignClient: CoreFeignClient

    private fun member(memberNo: String = "M001") = MemberValidateResponse(
        memberNo = memberNo, email = "test@example.com",
        memberStatusCode = "001", memberName = "홍길동"
    )

    private fun basket(
        basketNo: String, memberNo: String = "M001",
        goodsStatusCode: String = "001", itemStatusCode: String = "001",
        isOrder: Boolean = false, salePrice: Long = 10000L, quantity: Long = 2L, stock: Long = 10L
    ) = BasketResponse(
        basketNo = basketNo, memberNo = memberNo, goodsNo = "G001",
        goodsName = "테스트상품", goodsStatusCode = goodsStatusCode,
        itemNo = "001", itemName = "기본", itemStatusCode = itemStatusCode,
        salePrice = salePrice, quantity = quantity, stock = stock, isOrder = isOrder
    )

    @Test
    @DisplayName("주문서 조회 성공")
    fun getOrderSheet_Success() {
        val email = "test@example.com"
        val basketNos = listOf("B001", "B002")
        val baskets = listOf(basket("B001", salePrice = 10000L, quantity = 2L), basket("B002", salePrice = 15000L, quantity = 1L))

        given(coreFeignClient.getMemberByEmail(email)).willReturn(ApiResponse.success(member()))
        given(coreFeignClient.getBasketsByNos(basketNos)).willReturn(ApiResponse.success(baskets))

        val result = orderSheetService.getOrderSheet(email, basketNos)

        assertThat(result.ordererName).isEqualTo("홍길동")
        assertThat(result.items).hasSize(2)
        assertThat(result.totalProductAmount).isEqualTo(35000L)
        assertThat(result.totalQuantity).isEqualTo(3L)
    }

    @Test
    @DisplayName("장바구니 번호 없으면 예외")
    fun getOrderSheet_EmptyBasketNos() {
        assertThatThrownBy { orderSheetService.getOrderSheet("test@example.com", emptyList()) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("장바구니 번호가 필요합니다")
    }

    @Test
    @DisplayName("회원 정보 없으면 예외")
    fun getOrderSheet_MemberNotFound() {
        val email = "test@example.com"
        given(coreFeignClient.getMemberByEmail(email)).willReturn(ApiResponse.success())

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("회원 정보를 찾을 수 없습니다")
    }

    @Test
    @DisplayName("장바구니 없으면 예외")
    fun getOrderSheet_BasketNotFound() {
        val email = "test@example.com"
        given(coreFeignClient.getMemberByEmail(email)).willReturn(ApiResponse.success(member()))
        given(coreFeignClient.getBasketsByNos(listOf("B001"))).willReturn(ApiResponse.success(emptyList()))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("유효하지 않은 장바구니입니다")
    }

    @Test
    @DisplayName("다른 회원 장바구니면 예외")
    fun getOrderSheet_ForbiddenBasket() {
        val email = "test@example.com"
        given(coreFeignClient.getMemberByEmail(email)).willReturn(ApiResponse.success(member("M001")))
        given(coreFeignClient.getBasketsByNos(listOf("B001"))).willReturn(ApiResponse.success(listOf(basket("B001", memberNo = "M002"))))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("본인의 장바구니만 주문할 수 있습니다")
    }

    @Test
    @DisplayName("이미 주문된 장바구니면 예외")
    fun getOrderSheet_AlreadyOrdered() {
        val email = "test@example.com"
        given(coreFeignClient.getMemberByEmail(email)).willReturn(ApiResponse.success(member()))
        given(coreFeignClient.getBasketsByNos(listOf("B001"))).willReturn(ApiResponse.success(listOf(basket("B001", isOrder = true))))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("이미 주문된 상품입니다")
    }

    @Test
    @DisplayName("재고 부족하면 예외")
    fun getOrderSheet_InsufficientStock() {
        val email = "test@example.com"
        given(coreFeignClient.getMemberByEmail(email)).willReturn(ApiResponse.success(member()))
        given(coreFeignClient.getBasketsByNos(listOf("B001"))).willReturn(ApiResponse.success(listOf(basket("B001", quantity = 10L, stock = 5L))))

        assertThatThrownBy { orderSheetService.getOrderSheet(email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("재고가 부족합니다")
    }
}
