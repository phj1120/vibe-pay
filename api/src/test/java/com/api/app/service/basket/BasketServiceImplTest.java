package com.api.app.service.basket;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.dto.request.basket.BasketAddRequest;
import com.api.app.dto.request.basket.BasketModifyRequest;
import com.api.app.dto.response.basket.BasketResponse;
import com.api.app.entity.BasketBase;
import com.api.app.entity.MemberBase;
import com.api.app.repository.basket.BasketBaseMapper;
import com.api.app.repository.basket.BasketBaseTrxMapper;
import com.api.app.repository.member.MemberBaseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
class BasketServiceImplTest {

    @InjectMocks
    private BasketServiceImpl basketService;

    @Mock
    private BasketBaseMapper basketBaseMapper;

    @Mock
    private BasketBaseTrxMapper basketBaseTrxMapper;

    @Mock
    private MemberBaseMapper memberBaseMapper;

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getBasketList_Success() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);
        member.setEmail(email);

        List<BasketResponse> basketList = new ArrayList<>();
        BasketResponse basket1 = new BasketResponse();
        basket1.setBasketNo("000000000000001");
        basket1.setGoodsNo("G00000000000001");
        basket1.setItemNo("001");
        basket1.setQuantity(2L);
        basketList.add(basket1);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketListByMemberNo(memberNo)).willReturn(basketList);

        // when
        List<BasketResponse> result = basketService.getBasketList(email);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBasketNo()).isEqualTo("000000000000001");
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, times(1)).selectBasketListByMemberNo(memberNo);
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 새로운 상품")
    void addBasket_Success_NewItem() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";
        String basketNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        BasketAddRequest request = new BasketAddRequest();
        request.setGoodsNo("G00000000000001");
        request.setItemNo("001");
        request.setQuantity(1L);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketBaseByMemberAndItem(memberNo, request.getGoodsNo(), request.getItemNo()))
                .willReturn(null);
        given(basketBaseTrxMapper.generateBasketNo()).willReturn(basketNo);
        given(basketBaseTrxMapper.insertBasketBase(any(BasketBase.class))).willReturn(1);

        // when
        String result = basketService.addBasket(email, request);

        // then
        assertThat(result).isEqualTo(basketNo);
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, times(1)).selectBasketBaseByMemberAndItem(memberNo, request.getGoodsNo(), request.getItemNo());
        verify(basketBaseTrxMapper, times(1)).generateBasketNo();
        verify(basketBaseTrxMapper, times(1)).insertBasketBase(any(BasketBase.class));
    }

    @Test
    @DisplayName("장바구니 추가 성공 - 기존 상품 수량 증가")
    void addBasket_Success_UpdateQuantity() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";
        String basketNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        BasketAddRequest request = new BasketAddRequest();
        request.setGoodsNo("G00000000000001");
        request.setItemNo("001");
        request.setQuantity(2L);

        BasketBase existingBasket = new BasketBase();
        existingBasket.setBasketNo(basketNo);
        existingBasket.setMemberNo(memberNo);
        existingBasket.setGoodsNo(request.getGoodsNo());
        existingBasket.setItemNo(request.getItemNo());
        existingBasket.setQuantity(3L);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketBaseByMemberAndItem(memberNo, request.getGoodsNo(), request.getItemNo()))
                .willReturn(existingBasket);
        given(basketBaseTrxMapper.updateBasketBase(any(BasketBase.class))).willReturn(1);

        // when
        String result = basketService.addBasket(email, request);

        // then
        assertThat(result).isEqualTo(basketNo);
        assertThat(existingBasket.getQuantity()).isEqualTo(5L); // 3 + 2
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseTrxMapper, times(1)).updateBasketBase(any(BasketBase.class));
        verify(basketBaseTrxMapper, never()).insertBasketBase(any(BasketBase.class));
    }

    @Test
    @DisplayName("장바구니 수정 성공")
    void modifyBasket_Success() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";
        String basketNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        BasketBase basketBase = new BasketBase();
        basketBase.setBasketNo(basketNo);
        basketBase.setMemberNo(memberNo);
        basketBase.setQuantity(1L);

        BasketModifyRequest request = new BasketModifyRequest();
        request.setQuantity(5L);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketBaseByBasketNo(basketNo)).willReturn(basketBase);
        given(basketBaseTrxMapper.updateBasketBase(any(BasketBase.class))).willReturn(1);

        // when
        basketService.modifyBasket(email, basketNo, request);

        // then
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, times(1)).selectBasketBaseByBasketNo(basketNo);
        verify(basketBaseTrxMapper, times(1)).updateBasketBase(any(BasketBase.class));
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 장바구니 없음")
    void modifyBasket_Fail_BasketNotFound() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";
        String basketNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        BasketModifyRequest request = new BasketModifyRequest();
        request.setQuantity(5L);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketBaseByBasketNo(basketNo)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> basketService.modifyBasket(email, basketNo, request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, times(1)).selectBasketBaseByBasketNo(basketNo);
        verify(basketBaseTrxMapper, never()).updateBasketBase(any(BasketBase.class));
    }

    @Test
    @DisplayName("장바구니 수정 실패 - 권한 없음")
    void modifyBasket_Fail_Forbidden() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";
        String basketNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        BasketBase basketBase = new BasketBase();
        basketBase.setBasketNo(basketNo);
        basketBase.setMemberNo("000000000000002"); // 다른 회원의 장바구니

        BasketModifyRequest request = new BasketModifyRequest();
        request.setQuantity(5L);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketBaseByBasketNo(basketNo)).willReturn(basketBase);

        // when & then
        assertThatThrownBy(() -> basketService.modifyBasket(email, basketNo, request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.FORBIDDEN);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, times(1)).selectBasketBaseByBasketNo(basketNo);
        verify(basketBaseTrxMapper, never()).updateBasketBase(any(BasketBase.class));
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    void deleteBasket_Success() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";
        String basketNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        BasketBase basketBase = new BasketBase();
        basketBase.setBasketNo(basketNo);
        basketBase.setMemberNo(memberNo);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketBaseByBasketNo(basketNo)).willReturn(basketBase);
        given(basketBaseTrxMapper.deleteBasketBase(basketNo)).willReturn(1);

        // when
        basketService.deleteBasket(email, basketNo);

        // then
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, times(1)).selectBasketBaseByBasketNo(basketNo);
        verify(basketBaseTrxMapper, times(1)).deleteBasketBase(basketNo);
    }

    @Test
    @DisplayName("장바구니 여러 개 삭제 성공")
    void deleteBaskets_Success() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";
        List<String> basketNos = Arrays.asList("000000000000001", "000000000000002");

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        BasketBase basket1 = new BasketBase();
        basket1.setBasketNo("000000000000001");
        basket1.setMemberNo(memberNo);

        BasketBase basket2 = new BasketBase();
        basket2.setBasketNo("000000000000002");
        basket2.setMemberNo(memberNo);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketBaseByBasketNo("000000000000001")).willReturn(basket1);
        given(basketBaseMapper.selectBasketBaseByBasketNo("000000000000002")).willReturn(basket2);
        given(basketBaseTrxMapper.deleteBasketBaseByNos(basketNos)).willReturn(2);

        // when
        basketService.deleteBaskets(email, basketNos);

        // then
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, times(2)).selectBasketBaseByBasketNo(anyString());
        verify(basketBaseTrxMapper, times(1)).deleteBasketBaseByNos(basketNos);
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    void deleteAllBaskets_Success() {
        // given
        String email = "test@example.com";
        String memberNo = "000000000000001";

        MemberBase member = new MemberBase();
        member.setMemberNo(memberNo);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseTrxMapper.deleteBasketBaseByMemberNo(memberNo)).willReturn(5);

        // when
        basketService.deleteAllBaskets(email);

        // then
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseTrxMapper, times(1)).deleteBasketBaseByMemberNo(memberNo);
    }

    @Test
    @DisplayName("장바구니 조회 실패 - 회원 정보 없음")
    void getBasketList_Fail_MemberNotFound() {
        // given
        String email = "test@example.com";

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> basketService.getBasketList(email))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(basketBaseMapper, never()).selectBasketListByMemberNo(anyString());
    }
}
