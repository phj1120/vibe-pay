package com.api.app.service.order;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.dto.response.basket.BasketResponse;
import com.api.app.dto.response.order.OrderSheetResponse;
import com.api.app.emum.PRD001;
import com.api.app.entity.MemberBase;
import com.api.app.repository.basket.BasketBaseMapper;
import com.api.app.repository.member.MemberBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 주문서 서비스 구현
 *
 * @author system
 * @version 1.0
 * @since 2025-10-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSheetServiceImpl implements OrderSheetService {

    private final BasketBaseMapper basketBaseMapper;
    private final MemberBaseMapper memberBaseMapper;

    @Override
    public OrderSheetResponse getOrderSheet(String email, List<String> basketNos) {
        log.debug("주문서 정보 조회 시작: email={}, basketNos={}", email, basketNos);

        // 1. 파라미터 검증
        if (basketNos == null || basketNos.isEmpty()) {
            throw new ApiException(ApiError.INVALID_PARAMETER, "장바구니 번호가 필요합니다");
        }

        // 2. 회원 정보 조회
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);
        if (member == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }

        // 3. 장바구니 목록 조회
        List<BasketResponse> basketList = basketBaseMapper.selectBasketListByBasketNos(basketNos);

        // 4. 장바구니 유효성 검증
        if (basketList == null || basketList.isEmpty()) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "유효하지 않은 장바구니입니다");
        }

        if (basketList.size() != basketNos.size()) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "일부 장바구니를 찾을 수 없습니다");
        }

        // 5. 각 장바구니 항목 검증
        String memberNo = member.getMemberNo();
        for (BasketResponse basket : basketList) {
            // 소유권 확인
            if (!memberNo.equals(basket.getMemberNo())) {
                throw new ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 주문할 수 있습니다");
            }

            // 이미 주문된 장바구니인지 확인
            if (basket.getIsOrder() != null && basket.getIsOrder()) {
                throw new ApiException(ApiError.INVALID_PARAMETER,
                        String.format("이미 주문된 상품입니다: %s", basket.getGoodsName()));
            }

            // 상품 상태 확인 (상품 또는 단품이 판매 중인지)
            if (!PRD001.ON_SALE.isEquals(basket.getGoodsStatusCode())
                    || !PRD001.ON_SALE.isEquals(basket.getItemStatusCode())) {
                throw new ApiException(ApiError.INVALID_PARAMETER,
                        String.format("판매 중인 상품만 주문할 수 있습니다: %s", basket.getGoodsName()));
            }

            // 재고 확인
            if (basket.getStock() == null || basket.getStock() < basket.getQuantity()) {
                throw new ApiException(ApiError.INVALID_PARAMETER,
                        String.format("재고가 부족합니다: %s (재고: %d, 주문수량: %d)",
                                basket.getGoodsName(),
                                basket.getStock() != null ? basket.getStock() : 0,
                                basket.getQuantity()));
            }
        }

        // 6. 총 금액 및 수량 계산
        long totalProductAmount = basketList.stream()
                .mapToLong(basket -> basket.getSalePrice() * basket.getQuantity())
                .sum();

        long totalQuantity = basketList.stream()
                .mapToLong(BasketResponse::getQuantity)
                .sum();

        // 7. 응답 생성
        OrderSheetResponse response = new OrderSheetResponse();
        response.setItems(basketList);
        response.setOrdererName(member.getMemberName());
        response.setOrdererEmail(member.getEmail());
        response.setOrdererPhone(member.getPhone());
        response.setTotalProductAmount(totalProductAmount);
        response.setTotalQuantity(totalQuantity);

        log.info("주문서 정보 조회 완료: email={}, items={}, totalAmount={}",
                email, basketList.size(), totalProductAmount);

        return response;
    }
}
