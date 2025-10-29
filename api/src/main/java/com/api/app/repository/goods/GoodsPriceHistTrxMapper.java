package com.api.app.repository.goods;

import com.api.app.entity.GoodsPriceHist;

/**
 * 상품 가격 이력 등록/수정 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface GoodsPriceHistTrxMapper {

    /**
     * 상품 가격 이력 등록
     *
     * @param goodsPriceHist 가격 이력 정보
     * @return 등록된 건수
     */
    int insertGoodsPriceHist(GoodsPriceHist goodsPriceHist);

    /**
     * 이전 가격 이력 종료일 업데이트
     *
     * @param goodsNo 상품번호
     * @return 수정된 건수
     */
    int updatePreviousPriceEndDateTime(String goodsNo);
}
