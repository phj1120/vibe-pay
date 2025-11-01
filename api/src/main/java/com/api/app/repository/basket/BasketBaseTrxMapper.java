package com.api.app.repository.basket;

import com.api.app.entity.BasketBase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface BasketBaseTrxMapper {

    /**
     * 장바구니번호 시퀀스 생성
     *
     * @return 생성된 장바구니번호
     */
    String generateBasketNo();

    /**
     * 장바구니 등록
     *
     * @param basketBase 장바구니 정보
     * @return 등록된 건수
     */
    int insertBasketBase(BasketBase basketBase);

    /**
     * 장바구니 수정
     *
     * @param basketBase 장바구니 정보
     * @return 수정된 건수
     */
    int updateBasketBase(BasketBase basketBase);

    /**
     * 장바구니 삭제
     *
     * @param basketNo 장바구니번호
     * @return 삭제된 건수
     */
    int deleteBasketBase(String basketNo);

    /**
     * 장바구니 여러 개 삭제
     *
     * @param basketNos 장바구니번호 목록
     * @return 삭제된 건수
     */
    int deleteBasketBaseByNos(@Param("basketNos") List<String> basketNos);

    /**
     * 회원의 모든 장바구니 삭제
     *
     * @param memberNo 회원번호
     * @return 삭제된 건수
     */
    int deleteBasketBaseByMemberNo(String memberNo);

    /**
     * 장바구니 주문 완료 처리
     *
     * @param basketNo 장바구니번호
     * @param modifyId 수정자
     * @return 수정된 건수
     */
    int updateBasketIsOrder(@Param("basketNo") String basketNo, @Param("modifyId") String modifyId);
}
