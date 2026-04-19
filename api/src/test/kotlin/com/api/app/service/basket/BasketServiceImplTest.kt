package com.api.app.service.basket

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.basket.BasketAddRequest
import com.api.app.dto.request.basket.BasketModifyRequest
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
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

    private fun createMember(memberNo: String = "000000000000001", email: String = "test@example.com") =
        MemberBase().apply {
            this.memberNo = memberNo
            this.email = email
        }

    private fun mockProjection(basketNo: String, memberNo: String = "000000000000001"): BasketProjection {
        val p = mock(BasketProjection::class.java)
        given(p.getBasketNo()).willReturn(basketNo)
        given(p.getMemberNo()).willReturn(memberNo)
        given(p.getGoodsNo()).willReturn("G00000000000001")
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

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.selectBasketListByMemberNo(memberNo))
            .willReturn(listOf(mockProjection("000000000000001")))

        val result = basketService.getBasketList(email)

        assertThat(result).hasSize(1)
        assertThat(result[0].basketNo).isEqualTo("000000000000001")
        verify(memberBaseRepository, times(1)).findByEmail(email)
        verify(basketBaseRepository, times(1)).selectBasketListByMemberNo(memberNo)
    }

    @Test
    @DisplayName("장바구니 조회 실패 - 회원 정보 없음")
    fun getBasketList_Fail_MemberNotFound() {
        given(memberBaseRepository.findByEmail(anyString())).willReturn(null)

        assertThatThrownBy { basketService.getBasketList("test@example.com") }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)

        verify(basketBaseRepository, never()).selectBasketListByMemberNo(anyString())
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 새로운 상품")
    fun addBasket_Success_NewItem() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val request = BasketAddRequest(goodsNo = "G00000000000001", itemNo = "001", quantity = 1L)

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(memberNo, request.goodsNo, request.itemNo))
            .willReturn(null)
        given(basketBaseTrxRepository.save(any(BasketBase::class.java))).willAnswer { it.arguments[0] as BasketBase }

        basketService.addBasket(email, request)

        verify(memberBaseRepository, times(1)).findByEmail(email)
        verify(basketBaseRepository, times(1))
            .findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(memberNo, request.goodsNo, request.itemNo)
        verify(basketBaseTrxRepository, times(1)).save(any(BasketBase::class.java))
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 기존 상품 수량 증가")
    fun addBasket_Success_UpdateQuantity() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val request = BasketAddRequest(goodsNo = "G00000000000001", itemNo = "001", quantity = 2L)

        val existingBasket = BasketBase().apply {
            this.basketNo = "000000000000001"
            this.memberNo = memberNo
            this.goodsNo = request.goodsNo
            this.itemNo = request.itemNo
            this.quantity = 3L
        }

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.findByMemberNoAndGoodsNoAndItemNoAndIsOrderFalse(memberNo, request.goodsNo, request.itemNo))
            .willReturn(existingBasket)
        given(basketBaseTrxRepository.save(any(BasketBase::class.java))).willAnswer { it.arguments[0] as BasketBase }

        val result = basketService.addBasket(email, request)

        assertThat(result).isEqualTo("000000000000001")
        assertThat(existingBasket.quantity).isEqualTo(5L)
        verify(basketBaseTrxRepository, times(1)).save(any(BasketBase::class.java))
    }

    @Test
    @DisplayName("장바구니 수정 성공")
    fun modifyBasket_Success() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val basketNo = "000000000000001"
        val request = BasketModifyRequest(quantity = 5L)

        val basket = BasketBase().apply {
            this.basketNo = basketNo
            this.memberNo = memberNo
            this.quantity = 1L
        }

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.findById(basketNo)).willReturn(Optional.of(basket))
        given(basketBaseTrxRepository.save(any(BasketBase::class.java))).willAnswer { it.arguments[0] as BasketBase }

        basketService.modifyBasket(email, basketNo, request)

        verify(memberBaseRepository, times(1)).findByEmail(email)
        verify(basketBaseRepository, times(1)).findById(basketNo)
        verify(basketBaseTrxRepository, times(1)).save(any(BasketBase::class.java))
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 장바구니 없음")
    fun modifyBasket_Fail_BasketNotFound() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val basketNo = "000000000000001"
        val request = BasketModifyRequest(quantity = 5L)

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.findById(basketNo)).willReturn(Optional.empty())

        assertThatThrownBy { basketService.modifyBasket(email, basketNo, request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND)

        verify(basketBaseTrxRepository, never()).save(any(BasketBase::class.java))
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 권한 없음")
    fun modifyBasket_Fail_Forbidden() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val basketNo = "000000000000001"
        val request = BasketModifyRequest(quantity = 5L)

        val basket = BasketBase().apply {
            this.basketNo = basketNo
            this.memberNo = "000000000000002"
        }

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.findById(basketNo)).willReturn(Optional.of(basket))

        assertThatThrownBy { basketService.modifyBasket(email, basketNo, request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.FORBIDDEN)

        verify(basketBaseTrxRepository, never()).save(any(BasketBase::class.java))
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    fun deleteBasket_Success() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val basketNo = "000000000000001"

        val basket = BasketBase().apply {
            this.basketNo = basketNo
            this.memberNo = memberNo
        }

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.findById(basketNo)).willReturn(Optional.of(basket))

        basketService.deleteBasket(email, basketNo)

        verify(memberBaseRepository, times(1)).findByEmail(email)
        verify(basketBaseRepository, times(1)).findById(basketNo)
        verify(basketBaseTrxRepository, times(1)).deleteById(basketNo)
    }

    @Test
    @DisplayName("장바구니 여러 개 삭제 성공")
    fun deleteBaskets_Success() {
        val email = "test@example.com"
        val memberNo = "000000000000001"
        val basketNos = listOf("000000000000001", "000000000000002")

        val basket1 = BasketBase().apply { this.basketNo = "000000000000001"; this.memberNo = memberNo }
        val basket2 = BasketBase().apply { this.basketNo = "000000000000002"; this.memberNo = memberNo }

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseRepository.findById("000000000000001")).willReturn(Optional.of(basket1))
        given(basketBaseRepository.findById("000000000000002")).willReturn(Optional.of(basket2))
        given(basketBaseTrxRepository.deleteByBasketNoIn(anyList())).willReturn(2)

        basketService.deleteBaskets(email, basketNos)

        verify(memberBaseRepository, times(1)).findByEmail(email)
        verify(basketBaseRepository, times(2)).findById(anyString())
        verify(basketBaseTrxRepository, times(1)).deleteByBasketNoIn(basketNos)
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    fun deleteAllBaskets_Success() {
        val email = "test@example.com"
        val memberNo = "000000000000001"

        given(memberBaseRepository.findByEmail(email)).willReturn(createMember(memberNo, email))
        given(basketBaseTrxRepository.deleteByMemberNo(memberNo)).willReturn(5)

        basketService.deleteAllBaskets(email)

        verify(memberBaseRepository, times(1)).findByEmail(email)
        verify(basketBaseTrxRepository, times(1)).deleteByMemberNo(memberNo)
    }
}
