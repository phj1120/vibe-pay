package com.api.app.service.basket

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.basket.BasketAddRequest
import com.api.app.dto.request.basket.BasketModifyRequest
import com.api.app.emum.PRD001
import com.api.app.entity.BasketBase
import com.api.app.entity.MemberBase
import com.api.app.repository.rodb.basket.BasketBaseRepository
import com.api.app.repository.rodb.basket.BasketProjection
import com.api.app.repository.rodb.member.MemberBaseRepository
import com.api.app.repository.rwdb.basket.BasketBaseTrxRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BasketServiceImplTest {

    @InjectMocks
    private lateinit var basketService: BasketServiceImpl

    @Mock
    private lateinit var basketBaseRepository: BasketBaseRepository

    @Mock
    private lateinit var basketBaseTrxRepository: BasketBaseTrxRepository

    @Mock
    private lateinit var memberBaseRepository: MemberBaseRepository

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    fun getBasketListSuccess() {
        val email = "test@example.com"
        val member = createMember(email = email)
        val projection = createProjection(basketNo = "B001", memberNo = member.memberNo)
        given(memberBaseRepository.findByEmail(email)).willReturn(member)
        given(basketBaseRepository.selectBasketListByMemberNo(member.memberNo)).willReturn(listOf(projection))

        val result = basketService.getBasketList(email)

        assertThat(result).hasSize(1)
        assertThat(result.single().basketNo).isEqualTo("B001")
        assertThat(result.single().goodsStatusCode).isEqualTo(PRD001.ON_SALE.code)
        verify(basketBaseRepository, times(1)).selectBasketListByMemberNo(member.memberNo)
    }

    @Test
    @DisplayName("장바구니 목록 조회 실패 - 회원 없음")
    fun getBasketListFailWhenMemberMissing() {
        given(memberBaseRepository.findByEmail("missing@example.com")).willReturn(null)

        assertThatThrownBy { basketService.getBasketList("missing@example.com") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 신규 상품")
    fun addBasketSuccessForNewBasket() {
        val email = "test@example.com"
        val member = createMember(email = email)
        val request = BasketAddRequest(goodsNo = "G001", itemNo = "001", quantity = 2L)
        given(memberBaseRepository.findByEmail(email)).willReturn(member)
        given(
            basketBaseRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(
                member.memberNo,
                request.goodsNo,
                request.itemNo
            )
        ).willReturn(null)
        given(basketBaseTrxRepository.save(any(BasketBase::class.java))).willAnswer { invocation ->
            (invocation.arguments[0] as BasketBase).apply { basketNo = "B001" }
        }

        val basketNo = basketService.addBasket(email, request)

        val captor = ArgumentCaptor.forClass(BasketBase::class.java)
        verify(basketBaseTrxRepository).save(captor.capture())
        assertThat(basketNo).isEqualTo("B001")
        assertThat(captor.value.memberNo).isEqualTo(member.memberNo)
        assertThat(captor.value.isOrder).isFalse()
        assertThat(captor.value.quantity).isEqualTo(2L)
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 기존 수량 증가")
    fun addBasketSuccessForExistingBasket() {
        val email = "test@example.com"
        val member = createMember(email = email)
        val request = BasketAddRequest(goodsNo = "G001", itemNo = "001", quantity = 2L)
        val existing = BasketBase().apply {
            basketNo = "B001"
            memberNo = member.memberNo
            goodsNo = request.goodsNo
            itemNo = request.itemNo
            quantity = 3L
        }

        given(memberBaseRepository.findByEmail(email)).willReturn(member)
        given(
            basketBaseRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(
                member.memberNo,
                request.goodsNo,
                request.itemNo
            )
        ).willReturn(existing)

        val basketNo = basketService.addBasket(email, request)

        assertThat(basketNo).isEqualTo("B001")
        assertThat(existing.quantity).isEqualTo(5L)
        verify(basketBaseTrxRepository, times(1)).save(existing)
    }

    @Test
    @DisplayName("장바구니 수정 성공")
    fun modifyBasketSuccess() {
        val email = "test@example.com"
        val member = createMember(email = email)
        val basket = BasketBase().apply {
            basketNo = "B001"
            memberNo = member.memberNo
            goodsNo = "G001"
            itemNo = "001"
            quantity = 1L
        }
        val request = BasketModifyRequest(goodsNo = "G002", itemNo = "002", quantity = 5L)
        given(memberBaseRepository.findByEmail(email)).willReturn(member)
        given(basketBaseRepository.findById("B001")).willReturn(Optional.of(basket))

        basketService.modifyBasket(email, "B001", request)

        assertThat(basket.goodsNo).isEqualTo("G002")
        assertThat(basket.itemNo).isEqualTo("002")
        assertThat(basket.quantity).isEqualTo(5L)
        verify(basketBaseTrxRepository).save(basket)
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 장바구니 없음")
    fun modifyBasketFailWhenBasketMissing() {
        val member = createMember(email = "test@example.com")
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)
        given(basketBaseRepository.findById("B001")).willReturn(Optional.empty())

        assertThatThrownBy {
            basketService.modifyBasket(member.email, "B001", BasketModifyRequest(quantity = 1L))
        }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 권한 없음")
    fun modifyBasketFailWhenForbidden() {
        val member = createMember(email = "test@example.com")
        val basket = BasketBase().apply {
            basketNo = "B001"
            memberNo = "OTHER"
        }
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)
        given(basketBaseRepository.findById("B001")).willReturn(Optional.of(basket))

        assertThatThrownBy {
            basketService.modifyBasket(member.email, "B001", BasketModifyRequest(quantity = 1L))
        }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.FORBIDDEN)

        verify(basketBaseTrxRepository, never()).save(any(BasketBase::class.java))
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    fun deleteBasketSuccess() {
        val member = createMember(email = "test@example.com")
        val basket = BasketBase().apply {
            basketNo = "B001"
            memberNo = member.memberNo
        }
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)
        given(basketBaseRepository.findById("B001")).willReturn(Optional.of(basket))

        basketService.deleteBasket(member.email, "B001")

        verify(basketBaseTrxRepository).deleteById("B001")
    }

    @Test
    @DisplayName("장바구니 삭제 실패 - 권한 없음")
    fun deleteBasketFailWhenForbidden() {
        val member = createMember(email = "test@example.com")
        val basket = BasketBase().apply {
            basketNo = "B001"
            memberNo = "OTHER"
        }
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)
        given(basketBaseRepository.findById("B001")).willReturn(Optional.of(basket))

        assertThatThrownBy { basketService.deleteBasket(member.email, "B001") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.FORBIDDEN)

        verify(basketBaseTrxRepository, never()).deleteById("B001")
    }

    @Test
    @DisplayName("장바구니 여러 개 삭제 성공")
    fun deleteBasketsSuccess() {
        val member = createMember(email = "test@example.com")
        val basketNos = listOf("B001", "B002")
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)
        basketNos.forEach { basketNo ->
            given(basketBaseRepository.findById(basketNo)).willReturn(
                Optional.of(BasketBase().apply {
                    this.basketNo = basketNo
                    memberNo = member.memberNo
                })
            )
        }

        basketService.deleteBaskets(member.email, basketNos)

        verify(basketBaseTrxRepository).deleteByBasketNoIn(basketNos)
    }

    @Test
    @DisplayName("장바구니 여러 개 삭제 실패 - 비어 있는 요청")
    fun deleteBasketsFailWhenEmpty() {
        assertThatThrownBy { basketService.deleteBaskets("test@example.com", emptyList()) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("장바구니 여러 개 삭제 실패 - 일부 장바구니 없음")
    fun deleteBasketsFailWhenBasketMissing() {
        val member = createMember(email = "test@example.com")
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)
        given(basketBaseRepository.findById("B001")).willReturn(Optional.empty())

        assertThatThrownBy { basketService.deleteBaskets(member.email, listOf("B001")) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    fun deleteAllBasketsSuccess() {
        val member = createMember(email = "test@example.com")
        given(memberBaseRepository.findByEmail(member.email)).willReturn(member)

        basketService.deleteAllBaskets(member.email)

        verify(basketBaseTrxRepository).deleteByMemberNo(member.memberNo)
    }

    private fun createMember(
        memberNo: String = "M001",
        email: String = "test@example.com"
    ) = MemberBase().apply {
        this.memberNo = memberNo
        this.email = email
    }

    private fun createProjection(
        basketNo: String,
        memberNo: String = "M001"
    ): BasketProjection {
        val projection = mock(BasketProjection::class.java)
        doReturn(basketNo).`when`(projection).getBasketNo()
        doReturn(memberNo).`when`(projection).getMemberNo()
        doReturn("G001").`when`(projection).getGoodsNo()
        doReturn("테스트상품").`when`(projection).getGoodsName()
        doReturn(PRD001.ON_SALE.code).`when`(projection).getGoodsStatusCode()
        doReturn("https://cdn.example.com/goods.jpg").`when`(projection).getGoodsMainImageUrl()
        doReturn(10000L).`when`(projection).getSalePrice()
        doReturn("001").`when`(projection).getItemNo()
        doReturn("기본").`when`(projection).getItemName()
        doReturn(1000L).`when`(projection).getItemPrice()
        doReturn(PRD001.ON_SALE.code).`when`(projection).getItemStatusCode()
        doReturn(10L).`when`(projection).getStock()
        doReturn(2L).`when`(projection).getQuantity()
        doReturn(false).`when`(projection).getIsOrder()
        doReturn(LocalDateTime.of(2026, 4, 28, 0, 0)).`when`(projection).getRegistDateTime()
        return projection
    }
}
