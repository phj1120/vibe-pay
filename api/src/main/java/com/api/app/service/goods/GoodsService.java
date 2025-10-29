package com.api.app.service.goods;

import com.api.app.dto.request.goods.GoodsModifyRequest;
import com.api.app.dto.request.goods.GoodsRegisterRequest;
import com.api.app.dto.request.goods.GoodsSearchRequest;
import com.api.app.dto.response.goods.GoodsDetailResponse;
import com.api.app.dto.response.goods.GoodsPageResponse;

/**
 * 상품 서비스 인터페이스
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
public interface GoodsService {

    /**
     * 상품 등록
     *
     * @param request 상품 등록 요청
     * @return 생성된 상품번호
     */
    String registerGoods(GoodsRegisterRequest request);

    /**
     * 상품 수정
     *
     * @param goodsNo 상품번호
     * @param request 상품 수정 요청
     */
    void modifyGoods(String goodsNo, GoodsModifyRequest request);

    /**
     * 상품 목록 조회 (페이징)
     *
     * @param request 검색 조건
     * @return 상품 목록 (페이징)
     */
    GoodsPageResponse getGoodsList(GoodsSearchRequest request);

    /**
     * 상품 상세 조회
     *
     * @param goodsNo 상품번호
     * @return 상품 상세
     */
    GoodsDetailResponse getGoodsDetail(String goodsNo);

    /**
     * 상품 삭제
     *
     * @param goodsNo 상품번호
     */
    void deleteGoods(String goodsNo);
}
