package com.api.app.service.basket;

import com.api.app.dto.request.basket.BasketAddRequest;
import com.api.app.dto.request.basket.BasketModifyRequest;
import com.api.app.dto.response.basket.BasketResponse;

import java.util.List;

/**
 * 장바구니 서비스 인터페이스
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface BasketService {

    /**
     * 장바구니 목록 조회
     *
     * @param email 회원 이메일
     * @return 장바구니 목록
     */
    List<BasketResponse> getBasketList(String email);

    /**
     * 장바구니 추가
     * 동일한 상품/단품이 이미 있으면 수량을 증가시킴
     *
     * @param email   회원 이메일
     * @param request 장바구니 추가 요청
     * @return 생성된 장바구니번호
     */
    String addBasket(String email, BasketAddRequest request);

    /**
     * 장바구니 수정
     *
     * @param email     회원 이메일
     * @param basketNo  장바구니번호
     * @param request   장바구니 수정 요청
     */
    void modifyBasket(String email, String basketNo, BasketModifyRequest request);

    /**
     * 장바구니 삭제
     *
     * @param email    회원 이메일
     * @param basketNo 장바구니번호
     */
    void deleteBasket(String email, String basketNo);

    /**
     * 장바구니 여러 개 삭제
     *
     * @param email      회원 이메일
     * @param basketNos  장바구니번호 목록
     */
    void deleteBaskets(String email, List<String> basketNos);

    /**
     * 회원의 모든 장바구니 삭제
     *
     * @param email 회원 이메일
     */
    void deleteAllBaskets(String email);
}
