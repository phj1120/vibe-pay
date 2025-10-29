package com.api.app.repository.goods;

import com.api.app.entity.GoodsBase;

/**
 * 상품 등록/수정/삭제 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface GoodsBaseTrxMapper {

    /**
     * 상품번호 시퀀스 생성 (G + 14자리)
     *
     * @return 생성된 상품번호 (ex. G00000000000001)
     */
    String generateGoodsNo();

    /**
     * 상품 등록
     *
     * @param goodsBase 상품 정보
     * @return 등록된 건수
     */
    int insertGoodsBase(GoodsBase goodsBase);

    /**
     * 상품 수정
     *
     * @param goodsBase 상품 정보
     * @return 수정된 건수
     */
    int updateGoodsBase(GoodsBase goodsBase);

    /**
     * 상품 삭제
     *
     * @param goodsNo 상품번호
     * @return 삭제된 건수
     */
    int deleteGoodsBase(String goodsNo);
}
