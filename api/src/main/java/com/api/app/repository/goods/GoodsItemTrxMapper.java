package com.api.app.repository.goods;

import com.api.app.entity.GoodsItem;

/**
 * 단품 등록/수정/삭제 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface GoodsItemTrxMapper {

    /**
     * 단품 등록
     *
     * @param goodsItem 단품 정보
     * @return 등록된 건수
     */
    int insertGoodsItem(GoodsItem goodsItem);

    /**
     * 단품 수정
     *
     * @param goodsItem 단품 정보
     * @return 수정된 건수
     */
    int updateGoodsItem(GoodsItem goodsItem);

    /**
     * 상품의 모든 단품 삭제
     *
     * @param goodsNo 상품번호
     * @return 삭제된 건수
     */
    int deleteGoodsItemsByGoodsNo(String goodsNo);

    /**
     * 특정 단품 삭제
     *
     * @param goodsNo 상품번호
     * @param itemNo 단품번호
     * @return 삭제된 건수
     */
    int deleteGoodsItem(String goodsNo, String itemNo);

    /**
     * 단품번호 최대값 조회
     *
     * @param goodsNo 상품번호
     * @return 최대 단품번호
     */
    String selectMaxItemNo(String goodsNo);
}
