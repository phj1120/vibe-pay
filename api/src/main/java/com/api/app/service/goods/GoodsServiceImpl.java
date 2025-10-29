package com.api.app.service.goods;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.dto.request.goods.GoodsItemRequest;
import com.api.app.dto.request.goods.GoodsModifyRequest;
import com.api.app.dto.request.goods.GoodsRegisterRequest;
import com.api.app.dto.request.goods.GoodsSearchRequest;
import com.api.app.dto.response.goods.GoodsDetailResponse;
import com.api.app.dto.response.goods.GoodsItemResponse;
import com.api.app.dto.response.goods.GoodsListResponse;
import com.api.app.dto.response.goods.GoodsPageResponse;
import com.api.app.entity.GoodsBase;
import com.api.app.entity.GoodsItem;
import com.api.app.entity.GoodsPriceHist;
import com.api.app.repository.goods.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 서비스 구현
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsBaseMapper goodsBaseMapper;
    private final GoodsBaseTrxMapper goodsBaseTrxMapper;
    private final GoodsItemMapper goodsItemMapper;
    private final GoodsItemTrxMapper goodsItemTrxMapper;
    private final GoodsPriceHistMapper goodsPriceHistMapper;
    private final GoodsPriceHistTrxMapper goodsPriceHistTrxMapper;

    @Override
    @Transactional
    public String registerGoods(GoodsRegisterRequest request) {
        log.debug("상품 등록 시작: {}", request.getGoodsName());

        // 상품번호 생성
        String goodsNo = goodsBaseTrxMapper.generateGoodsNo();
        log.debug("상품번호 생성: {}", goodsNo);

        // 상품 기본 정보 등록
        GoodsBase goodsBase = new GoodsBase();
        goodsBase.setGoodsNo(goodsNo);
        goodsBase.setGoodsName(request.getGoodsName());
        goodsBase.setGoodsStatusCode(request.getGoodsStatusCode());
        goodsBase.setGoodsMainImageUrl(request.getGoodsMainImageUrl());

        int result = goodsBaseTrxMapper.insertGoodsBase(goodsBase);
        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "상품 등록에 실패했습니다");
        }

        // 상품 가격 이력 등록
        GoodsPriceHist priceHist = new GoodsPriceHist();
        priceHist.setGoodsNo(goodsNo);
        priceHist.setStartDateTime(LocalDateTime.now());
        priceHist.setEndDateTime(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
        priceHist.setSalePrice(request.getSalePrice());
        priceHist.setSupplyPrice(request.getSupplyPrice());

        result = goodsPriceHistTrxMapper.insertGoodsPriceHist(priceHist);
        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "가격 정보 등록에 실패했습니다");
        }

        // 단품 등록
        String itemNo = "001";
        for (GoodsItemRequest itemRequest : request.getItems()) {
            GoodsItem goodsItem = new GoodsItem();
            goodsItem.setGoodsNo(goodsNo);
            goodsItem.setItemNo(itemNo);
            goodsItem.setItemName(itemRequest.getItemName());
            goodsItem.setItemPrice(itemRequest.getItemPrice());
            goodsItem.setStock(itemRequest.getStock());
            goodsItem.setGoodsStatusCode(itemRequest.getGoodsStatusCode());

            result = goodsItemTrxMapper.insertGoodsItem(goodsItem);
            if (result != 1) {
                throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "단품 등록에 실패했습니다");
            }

            // 다음 단품번호 계산
            itemNo = String.format("%03d", Integer.parseInt(itemNo) + 1);
        }

        log.info("상품 등록 완료: goodsNo={}, goodsName={}", goodsNo, request.getGoodsName());
        return goodsNo;
    }

    @Override
    @Transactional
    public void modifyGoods(String goodsNo, GoodsModifyRequest request) {
        log.debug("상품 수정 시작: goodsNo={}", goodsNo);

        // 상품 존재 여부 확인
        int exists = goodsBaseMapper.existsGoodsByGoodsNo(goodsNo);
        if (exists == 0) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다");
        }

        // 상품 기본 정보 수정
        GoodsBase goodsBase = new GoodsBase();
        goodsBase.setGoodsNo(goodsNo);
        goodsBase.setGoodsName(request.getGoodsName());
        goodsBase.setGoodsStatusCode(request.getGoodsStatusCode());
        goodsBase.setGoodsMainImageUrl(request.getGoodsMainImageUrl());

        int result = goodsBaseTrxMapper.updateGoodsBase(goodsBase);
        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "상품 수정에 실패했습니다");
        }

        // 가격 변경 여부 확인
        GoodsPriceHist currentPrice = goodsPriceHistMapper.selectCurrentPrice(goodsNo);
        if (currentPrice == null ||
            !currentPrice.getSalePrice().equals(request.getSalePrice()) ||
            !currentPrice.getSupplyPrice().equals(request.getSupplyPrice())) {

            // 이전 가격 종료
            goodsPriceHistTrxMapper.updatePreviousPriceEndDateTime(goodsNo);

            // 새 가격 등록
            GoodsPriceHist priceHist = new GoodsPriceHist();
            priceHist.setGoodsNo(goodsNo);
            priceHist.setStartDateTime(LocalDateTime.now());
            priceHist.setEndDateTime(LocalDateTime.of(9999, 12, 31, 23, 59, 59));
            priceHist.setSalePrice(request.getSalePrice());
            priceHist.setSupplyPrice(request.getSupplyPrice());

            result = goodsPriceHistTrxMapper.insertGoodsPriceHist(priceHist);
            if (result != 1) {
                throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "가격 정보 등록에 실패했습니다");
            }
        }

        // 기존 단품 삭제
        goodsItemTrxMapper.deleteGoodsItemsByGoodsNo(goodsNo);

        // 단품 재등록
        String itemNo = "001";
        for (GoodsItemRequest itemRequest : request.getItems()) {
            GoodsItem goodsItem = new GoodsItem();
            goodsItem.setGoodsNo(goodsNo);
            goodsItem.setItemNo(itemNo);
            goodsItem.setItemName(itemRequest.getItemName());
            goodsItem.setItemPrice(itemRequest.getItemPrice());
            goodsItem.setStock(itemRequest.getStock());
            goodsItem.setGoodsStatusCode(itemRequest.getGoodsStatusCode());

            result = goodsItemTrxMapper.insertGoodsItem(goodsItem);
            if (result != 1) {
                throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "단품 등록에 실패했습니다");
            }

            // 다음 단품번호 계산
            itemNo = String.format("%03d", Integer.parseInt(itemNo) + 1);
        }

        log.info("상품 수정 완료: goodsNo={}", goodsNo);
    }

    @Override
    public GoodsPageResponse getGoodsList(GoodsSearchRequest request) {
        log.debug("상품 목록 조회: page={}, size={}", request.getPage(), request.getSize());

        // 상품 목록 조회
        List<GoodsListResponse> content = goodsBaseMapper.selectGoodsList(request);

        // 전체 개수 조회
        Long totalElements = goodsBaseMapper.countGoodsList(request);

        // 페이징 정보 계산
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        // 응답 생성
        GoodsPageResponse response = new GoodsPageResponse();
        response.setContent(content);
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setLast(request.getPage() >= totalPages - 1);

        log.debug("상품 목록 조회 완료: totalElements={}, totalPages={}", totalElements, totalPages);
        return response;
    }

    @Override
    public GoodsDetailResponse getGoodsDetail(String goodsNo) {
        log.debug("상품 상세 조회: goodsNo={}", goodsNo);

        // 상품 기본 정보 조회
        GoodsDetailResponse response = goodsBaseMapper.selectGoodsDetail(goodsNo);
        if (response == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다");
        }

        // 단품 목록 조회
        List<GoodsItemResponse> items = goodsItemMapper.selectGoodsItemsByGoodsNo(goodsNo);
        response.setItems(items);

        log.debug("상품 상세 조회 완료: goodsNo={}", goodsNo);
        return response;
    }

    @Override
    @Transactional
    public void deleteGoods(String goodsNo) {
        log.debug("상품 삭제 시작: goodsNo={}", goodsNo);

        // 상품 존재 여부 확인
        int exists = goodsBaseMapper.existsGoodsByGoodsNo(goodsNo);
        if (exists == 0) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "상품을 찾을 수 없습니다");
        }

        // 단품 삭제
        goodsItemTrxMapper.deleteGoodsItemsByGoodsNo(goodsNo);

        // 상품 삭제
        int result = goodsBaseTrxMapper.deleteGoodsBase(goodsNo);
        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "상품 삭제에 실패했습니다");
        }

        log.info("상품 삭제 완료: goodsNo={}", goodsNo);
    }
}
