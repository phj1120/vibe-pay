package com.api.app.service.order;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.dto.response.basket.BasketResponse;
import com.api.app.dto.response.order.OrderSheetResponse;
import com.api.app.entity.MemberBase;
import com.api.app.repository.basket.BasketBaseMapper;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-30
 */
@ExtendWith(MockitoExtension.class)
class OrderSheetServiceImplTest {

    @InjectMocks
    private OrderSheetServiceImpl orderSheetService;

    @Mock
    private BasketBaseMapper basketBaseMapper;

    @Mock
    private MemberBaseMapper memberBaseMapper;

    @Test
    @DisplayName("주문서 정보 조회 성공")
    void getOrderSheet_Success() {
        // given
        String email = "test@example.com";
        List<String> basketNos = Arrays.asList("B001", "B002");

        MemberBase member = new MemberBase();
        member.setMemberNo("M001");
        member.setEmail(email);
        member.setMemberName("홍길동");
        member.setPhone("010-1234-5678");

        BasketResponse basket1 = new BasketResponse();
        basket1.setBasketNo("B001");
        basket1.setMemberNo("M001");
        basket1.setGoodsNo("G001");
        basket1.setGoodsName("상품1");
        basket1.setGoodsStatusCode("SALE");
        basket1.setItemStatusCode("SALE");
        basket1.setSalePrice(10000L);
        basket1.setQuantity(2L);
        basket1.setStock(10L);
        basket1.setIsOrder(false);

        BasketResponse basket2 = new BasketResponse();
        basket2.setBasketNo("B002");
        basket2.setMemberNo("M001");
        basket2.setGoodsNo("G002");
        basket2.setGoodsName("상품2");
        basket2.setGoodsStatusCode("SALE");
        basket2.setItemStatusCode("SALE");
        basket2.setSalePrice(15000L);
        basket2.setQuantity(1L);
        basket2.setStock(5L);
        basket2.setIsOrder(false);

        List<BasketResponse> basketList = Arrays.asList(basket1, basket2);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketListByBasketNos(basketNos)).willReturn(basketList);

        // when
        OrderSheetResponse response = orderSheetService.getOrderSheet(email, basketNos);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrdererName()).isEqualTo("홍길동");
        assertThat(response.getOrdererEmail()).isEqualTo(email);
        assertThat(response.getOrdererPhone()).isEqualTo("010-1234-5678");
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getTotalProductAmount()).isEqualTo(35000L); // (10000*2) + (15000*1)
        assertThat(response.getTotalQuantity()).isEqualTo(3L); // 2 + 1
    }

    @Test
    @DisplayName("장바구니 번호가 없으면 예외 발생")
    void getOrderSheet_EmptyBasketNos() {
        // given
        String email = "test@example.com";
        List<String> basketNos = new ArrayList<>();

        // when & then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(email, basketNos))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("장바구니 번호가 필요합니다");
    }

    @Test
    @DisplayName("회원 정보가 없으면 예외 발생")
    void getOrderSheet_MemberNotFound() {
        // given
        String email = "test@example.com";
        List<String> basketNos = Arrays.asList("B001");

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(email, basketNos))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("회원 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("장바구니가 존재하지 않으면 예외 발생")
    void getOrderSheet_BasketNotFound() {
        // given
        String email = "test@example.com";
        List<String> basketNos = Arrays.asList("B001");

        MemberBase member = new MemberBase();
        member.setMemberNo("M001");
        member.setEmail(email);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketListByBasketNos(basketNos)).willReturn(new ArrayList<>());

        // when & then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(email, basketNos))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("유효하지 않은 장바구니입니다");
    }

    @Test
    @DisplayName("다른 회원의 장바구니면 예외 발생")
    void getOrderSheet_ForbiddenBasket() {
        // given
        String email = "test@example.com";
        List<String> basketNos = Arrays.asList("B001");

        MemberBase member = new MemberBase();
        member.setMemberNo("M001");
        member.setEmail(email);

        BasketResponse basket = new BasketResponse();
        basket.setBasketNo("B001");
        basket.setMemberNo("M002"); // 다른 회원의 장바구니
        basket.setGoodsStatusCode("SALE");
        basket.setItemStatusCode("SALE");
        basket.setIsOrder(false);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketListByBasketNos(basketNos)).willReturn(Arrays.asList(basket));

        // when & then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(email, basketNos))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("본인의 장바구니만 주문할 수 있습니다");
    }

    @Test
    @DisplayName("이미 주문된 장바구니면 예외 발생")
    void getOrderSheet_AlreadyOrdered() {
        // given
        String email = "test@example.com";
        List<String> basketNos = Arrays.asList("B001");

        MemberBase member = new MemberBase();
        member.setMemberNo("M001");
        member.setEmail(email);

        BasketResponse basket = new BasketResponse();
        basket.setBasketNo("B001");
        basket.setMemberNo("M001");
        basket.setGoodsName("상품1");
        basket.setGoodsStatusCode("SALE");
        basket.setItemStatusCode("SALE");
        basket.setIsOrder(true); // 이미 주문됨

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketListByBasketNos(basketNos)).willReturn(Arrays.asList(basket));

        // when & then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(email, basketNos))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("이미 주문된 상품입니다");
    }

    @Test
    @DisplayName("상품이 판매중이 아니면 예외 발생")
    void getOrderSheet_GoodsNotOnSale() {
        // given
        String email = "test@example.com";
        List<String> basketNos = Arrays.asList("B001");

        MemberBase member = new MemberBase();
        member.setMemberNo("M001");
        member.setEmail(email);

        BasketResponse basket = new BasketResponse();
        basket.setBasketNo("B001");
        basket.setMemberNo("M001");
        basket.setGoodsName("상품1");
        basket.setGoodsStatusCode("SOLD_OUT"); // 판매중이 아님
        basket.setItemStatusCode("SALE");
        basket.setIsOrder(false);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketListByBasketNos(basketNos)).willReturn(Arrays.asList(basket));

        // when & then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(email, basketNos))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("판매 중인 상품만 주문할 수 있습니다");
    }

    @Test
    @DisplayName("재고가 부족하면 예외 발생")
    void getOrderSheet_InsufficientStock() {
        // given
        String email = "test@example.com";
        List<String> basketNos = Arrays.asList("B001");

        MemberBase member = new MemberBase();
        member.setMemberNo("M001");
        member.setEmail(email);

        BasketResponse basket = new BasketResponse();
        basket.setBasketNo("B001");
        basket.setMemberNo("M001");
        basket.setGoodsName("상품1");
        basket.setGoodsStatusCode("SALE");
        basket.setItemStatusCode("SALE");
        basket.setQuantity(10L);
        basket.setStock(5L); // 재고 부족
        basket.setIsOrder(false);

        given(memberBaseMapper.selectMemberBaseByEmail(email)).willReturn(member);
        given(basketBaseMapper.selectBasketListByBasketNos(basketNos)).willReturn(Arrays.asList(basket));

        // when & then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(email, basketNos))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("재고가 부족합니다");
    }
}
