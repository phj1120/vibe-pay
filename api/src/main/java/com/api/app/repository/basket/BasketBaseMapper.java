package com.api.app.repository.basket;

import com.api.app.entity.BasketBase;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface BasketBaseMapper {

    /**
     * 장바구니 전체 조회
     *
     * @return 장바구니 목록
     */
    List<BasketBase> selectAllBasketBase();

    /**
     * 장바구니번호로 조회
     *
     * @param basketNo 장바구니번호
     * @return 장바구니 정보
     */
    BasketBase selectBasketBaseByBasketNo(String basketNo);

    /**
     * 회원번호로 조회
     *
     * @param memberNo 회원번호
     * @return 장바구니 목록
     */
    List<BasketBase> selectBasketBaseByMemberNo(String memberNo);
}
