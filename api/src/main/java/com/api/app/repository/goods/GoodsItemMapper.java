package com.api.app.repository.goods;

import com.api.app.dto.response.goods.GoodsItemResponse;
import com.api.app.entity.GoodsItem;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 상품번호와 단품번호로 단품 조회
     *
     * @param goodsNo 상품번호
     * @param itemNo 단품번호
     * @return 단품 엔티티
     */
    GoodsItem selectGoodsItemByKey(@Param("goodsNo") String goodsNo, @Param("itemNo") String itemNo);
}
