package com.api.app.repository.basket;

import com.api.app.dto.response.basket.BasketResponse;
import com.api.app.entity.BasketBase;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 회원번호로 장바구니 목록 조회 (상품/단품 정보 포함)
     *
     * @param memberNo 회원번호
     * @return 장바구니 목록
     */
    List<BasketResponse> selectBasketListByMemberNo(String memberNo);

    /**
     * 회원번호 + 상품번호 + 단품번호로 장바구니 조회
     *
     * @param memberNo 회원번호
     * @param goodsNo  상품번호
     * @param itemNo   단품번호
     * @return 장바구니 정보
     */
    BasketBase selectBasketBaseByMemberAndItem(
            @Param("memberNo") String memberNo,
            @Param("goodsNo") String goodsNo,
            @Param("itemNo") String itemNo
    );

    /**
     * 장바구니번호 목록으로 장바구니 목록 조회 (상품/단품 정보 포함)
     *
     * @param basketNos 장바구니번호 목록
     * @return 장바구니 목록
     */
    List<BasketResponse> selectBasketListByBasketNos(@Param("basketNos") List<String> basketNos);
}
