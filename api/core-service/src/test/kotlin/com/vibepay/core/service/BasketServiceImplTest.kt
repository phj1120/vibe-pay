package com.vibepay.core.service

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.vibepay.core.dto.request.basket.BasketAddRequest
import com.vibepay.core.dto.request.basket.BasketModifyRequest
import com.vibepay.core.entity.BasketBase
import com.vibepay.core.entity.MemberBase
import com.vibepay.core.repository.BasketProjection
import com.vibepay.core.repository.BasketRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BasketServiceImplTest {

    @InjectMocks
    private lateinit var basketService: BasketServiceImpl

    @Mock
    private lateinit var basketRepository: BasketRepository

    @Mock
    private lateinit var memberService: MemberService

    private fun member(memberNo: String = "000000000000001") = MemberBase().apply {
        this.memberNo = memberNo
        this.email = "test@example.com"
    }

    private fun mockProjection(basketNo: String, memberNo: String = "000000000000001"): BasketProjection {
        val p = mock(BasketProjection::class.java)
        given(p.getBasketNo()).willReturn(basketNo)
        given(p.getMemberNo()).willReturn(memberNo)
        given(p.getGoodsNo()).willReturn("G001")
        given(p.getGoodsName()).willReturn("테스트상품")
        given(p.getGoodsStatusCode()).willReturn("SALE")
        given(p.getGoodsMainImageUrl()).willReturn(null)
        given(p.getSalePrice()).willReturn(10000L)
        given(p.getItemNo()).willReturn("001")
        given(p.getItemName()).willReturn("기본")
        given(p.getItemPrice()).willReturn(0L)
        given(p.getItemStatusCode()).willReturn("SALE")
        given(p.getStock()).willReturn(10L)
        given(p.getQuantity()).willReturn(2L)
        given(p.getIsOrder()).willReturn(false)
        given(p.getRegistDateTime()).willReturn(null)
        return p
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    fun getBasketList_Success() {
        val email = "test@example.com"
        val memberNo = "000000000000001"

        given(memberService.findByEmail(email)).willReturn(member(memberNo))
        given(basketRepository.selectBasketListByMemberNo(memberNo)).willReturn(listOf(mockProjection("B001")))

        val result = basketService.getBasketList(email)

        assertThat(result).hasSize(1)
        assertThat(result[0].basketNo).isEqualTo("B001")
    }

    @Test
    @DisplayName("장바구니 조회 실패 - 회원 없음")
    fun getBasketList_Fail_MemberNotFound() {
        given(memberService.findByEmail(anyString())).willReturn(null)

        assertThatThrownBy { basketService.getBasketList("test@example.com") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 새 상품")
    fun addBasket_Success_NewItem() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val request = BasketAddRequest(goodsNo = "G001", itemNo = "001", quantity = 1L)

        given(memberService.findByEmail(email)).willReturn(member(memberNo))
        given(basketRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(memberNo, request.goodsNo, request.itemNo)).willReturn(null)
        given(basketRepository.save(any(BasketBase::class.java))).willAnswer { it.arguments[0] as BasketBase }

        basketService.addBasket(email, request)

        verify(basketRepository, times(1)).save(any(BasketBase::class.java))
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 기존 상품 수량 증가")
    fun addBasket_Success_UpdateQuantity() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val request = BasketAddRequest(goodsNo = "G001", itemNo = "001", quantity = 2L)
        val existing = BasketBase().apply { this.basketNo = "B001"; this.memberNo = memberNo; quantity = 3L }

        given(memberService.findByEmail(email)).willReturn(member(memberNo))
        given(basketRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(memberNo, request.goodsNo, request.itemNo)).willReturn(existing)
        given(basketRepository.save(any(BasketBase::class.java))).willAnswer { it.arguments[0] as BasketBase }

        val result = basketService.addBasket(email, request)

        assertThat(result).isEqualTo("B001")
        assertThat(existing.quantity).isEqualTo(5L)
    }

    @Test
    @DisplayName("장바구니 수정 성공")
    fun modifyBasket_Success() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val basket = BasketBase().apply { this.basketNo = "B001"; this.memberNo = memberNo; quantity = 1L }

        given(memberService.findByEmail(email)).willReturn(member(memberNo))
        given(basketRepository.findById("B001")).willReturn(Optional.of(basket))
        given(basketRepository.save(any(BasketBase::class.java))).willAnswer { it.arguments[0] as BasketBase }

        basketService.modifyBasket(email, "B001", BasketModifyRequest(quantity = 5L))

        assertThat(basket.quantity).isEqualTo(5L)
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 없음")
    fun modifyBasket_Fail_NotFound() {
        given(memberService.findByEmail(anyString())).willReturn(member())
        given(basketRepository.findById("B001")).willReturn(Optional.empty())

        assertThatThrownBy { basketService.modifyBasket("test@example.com", "B001", BasketModifyRequest(quantity = 5L)) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 권한 없음")
    fun modifyBasket_Fail_Forbidden() {
        val basket = BasketBase().apply { this.basketNo = "B001"; this.memberNo = "OTHER" }
        given(memberService.findByEmail(anyString())).willReturn(member("000000000000001"))
        given(basketRepository.findById("B001")).willReturn(Optional.of(basket))

        assertThatThrownBy { basketService.modifyBasket("test@example.com", "B001", BasketModifyRequest(quantity = 5L)) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.FORBIDDEN)
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    fun deleteBasket_Success() {
        val memberNo = "000000000000001"
        val basket = BasketBase().apply { this.basketNo = "B001"; this.memberNo = memberNo }

        given(memberService.findByEmail(anyString())).willReturn(member(memberNo))
        given(basketRepository.findById("B001")).willReturn(Optional.of(basket))

        basketService.deleteBasket("test@example.com", "B001")

        verify(basketRepository, times(1)).deleteById("B001")
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    fun deleteAllBaskets_Success() {
        val memberNo = "000000000000001"
        given(memberService.findByEmail(anyString())).willReturn(member(memberNo))

        basketService.deleteAllBaskets("test@example.com")

        verify(basketRepository, times(1)).deleteByMemberNo(memberNo)
    }
}
