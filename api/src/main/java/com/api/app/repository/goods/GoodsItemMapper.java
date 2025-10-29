package com.api.app.repository.goods;

import com.api.app.dto.response.goods.GoodsItemResponse;

import java.util.List;

/**
 * 단품 조회 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface GoodsItemMapper {

    /**
     * 상품의 단품 목록 조회
     *
     * @param goodsNo 상품번호
     * @return 단품 목록
     */
    List<GoodsItemResponse> selectGoodsItemsByGoodsNo(String goodsNo);
}
