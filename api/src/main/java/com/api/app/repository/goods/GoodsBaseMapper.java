package com.api.app.repository.goods;

import com.api.app.dto.request.goods.GoodsSearchRequest;
import com.api.app.dto.response.goods.GoodsDetailResponse;
import com.api.app.dto.response.goods.GoodsListResponse;

import java.util.List;

/**
 * 상품 조회 Mapper
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface GoodsBaseMapper {

    /**
     * 상품 목록 조회 (페이징)
     *
     * @param request 검색 조건
     * @return 상품 목록
     */
    List<GoodsListResponse> selectGoodsList(GoodsSearchRequest request);

    /**
     * 상품 목록 전체 개수 조회
     *
     * @param request 검색 조건
     * @return 전체 개수
     */
    Long countGoodsList(GoodsSearchRequest request);

    /**
     * 상품 상세 조회
     *
     * @param goodsNo 상품번호
     * @return 상품 상세
     */
    GoodsDetailResponse selectGoodsDetail(String goodsNo);

    /**
     * 상품 존재 여부 확인
     *
     * @param goodsNo 상품번호
     * @return 존재 여부
     */
    int existsGoodsByGoodsNo(String goodsNo);
}
