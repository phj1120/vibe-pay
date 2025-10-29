package com.api.app.repository.goods;

import com.api.app.entity.GoodsPriceHist;

/**
 * 상품 가격 이력 조회 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface GoodsPriceHistMapper {

    /**
     * 상품의 현재 유효한 가격 조회
     *
     * @param goodsNo 상품번호
     * @return 가격 정보
     */
    GoodsPriceHist selectCurrentPrice(String goodsNo);
}
