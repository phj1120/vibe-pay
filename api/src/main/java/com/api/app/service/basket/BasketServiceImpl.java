package com.api.app.service.basket;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.dto.request.basket.BasketAddRequest;
import com.api.app.dto.request.basket.BasketModifyRequest;
import com.api.app.dto.response.basket.BasketResponse;
import com.api.app.entity.BasketBase;
import com.api.app.entity.MemberBase;
import com.api.app.repository.basket.BasketBaseMapper;
import com.api.app.repository.basket.BasketBaseTrxMapper;
import com.api.app.repository.member.MemberBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 장바구니 서비스 구현
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {

    private final BasketBaseMapper basketBaseMapper;
    private final BasketBaseTrxMapper basketBaseTrxMapper;
    private final MemberBaseMapper memberBaseMapper;

    @Override
    public List<BasketResponse> getBasketList(String email) {
        log.debug("장바구니 목록 조회 시작: email={}", email);

        // 회원번호 조회
        String memberNo = getMemberNoByEmail(email);

        // 장바구니 목록 조회
        List<BasketResponse> basketList = basketBaseMapper.selectBasketListByMemberNo(memberNo);

        log.debug("장바구니 목록 조회 완료: email={}, count={}", email, basketList.size());
        return basketList;
    }

    @Override
    @Transactional
    public String addBasket(String email, BasketAddRequest request) {
        log.debug("장바구니 추가 시작: email={}, goodsNo={}, itemNo={}",
                email, request.getGoodsNo(), request.getItemNo());

        // 회원번호 조회
        String memberNo = getMemberNoByEmail(email);

        // 동일한 상품/단품이 이미 있는지 확인
        BasketBase existingBasket = basketBaseMapper.selectBasketBaseByMemberAndItem(
                memberNo, request.getGoodsNo(), request.getItemNo());

        String basketNo;

        if (existingBasket != null) {
            // 이미 있으면 수량 증가
            existingBasket.setQuantity(existingBasket.getQuantity() + request.getQuantity());
            existingBasket.setModifyId(memberNo);

            int result = basketBaseTrxMapper.updateBasketBase(existingBasket);
            if (result != 1) {
                throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "장바구니 수량 수정에 실패했습니다");
            }

            basketNo = existingBasket.getBasketNo();
            log.info("장바구니 수량 증가 완료: basketNo={}, newQuantity={}",
                    basketNo, existingBasket.getQuantity());

        } else {
            // 없으면 새로 추가
            basketNo = basketBaseTrxMapper.generateBasketNo();
            log.debug("장바구니번호 생성: {}", basketNo);

            BasketBase basketBase = new BasketBase();
            basketBase.setBasketNo(basketNo);
            basketBase.setMemberNo(memberNo);
            basketBase.setGoodsNo(request.getGoodsNo());
            basketBase.setItemNo(request.getItemNo());
            basketBase.setQuantity(request.getQuantity());
            basketBase.setIsOrder(false);
            basketBase.setRegistId(memberNo);

            int result = basketBaseTrxMapper.insertBasketBase(basketBase);
            if (result != 1) {
                throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "장바구니 추가에 실패했습니다");
            }

            log.info("장바구니 추가 완료: basketNo={}, goodsNo={}, itemNo={}",
                    basketNo, request.getGoodsNo(), request.getItemNo());
        }

        return basketNo;
    }

    @Override
    @Transactional
    public void modifyBasket(String email, String basketNo, BasketModifyRequest request) {
        log.debug("장바구니 수정 시작: email={}, basketNo={}", email, basketNo);

        // 회원번호 조회
        String memberNo = getMemberNoByEmail(email);

        // 장바구니 조회 및 권한 확인
        BasketBase basketBase = basketBaseMapper.selectBasketBaseByBasketNo(basketNo);
        if (basketBase == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다");
        }

        if (!basketBase.getMemberNo().equals(memberNo)) {
            throw new ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 수정할 수 있습니다");
        }

        // 수정할 필드 적용
        if (request.getGoodsNo() != null) {
            basketBase.setGoodsNo(request.getGoodsNo());
        }
        if (request.getItemNo() != null) {
            basketBase.setItemNo(request.getItemNo());
        }
        if (request.getQuantity() != null) {
            basketBase.setQuantity(request.getQuantity());
        }
        basketBase.setModifyId(memberNo);

        int result = basketBaseTrxMapper.updateBasketBase(basketBase);
        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "장바구니 수정에 실패했습니다");
        }

        log.info("장바구니 수정 완료: basketNo={}", basketNo);
    }

    @Override
    @Transactional
    public void deleteBasket(String email, String basketNo) {
        log.debug("장바구니 삭제 시작: email={}, basketNo={}", email, basketNo);

        // 회원번호 조회
        String memberNo = getMemberNoByEmail(email);

        // 장바구니 조회 및 권한 확인
        BasketBase basketBase = basketBaseMapper.selectBasketBaseByBasketNo(basketNo);
        if (basketBase == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "장바구니를 찾을 수 없습니다");
        }

        if (!basketBase.getMemberNo().equals(memberNo)) {
            throw new ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 삭제할 수 있습니다");
        }

        // 장바구니 삭제
        int result = basketBaseTrxMapper.deleteBasketBase(basketNo);
        if (result != 1) {
            throw new ApiException(ApiError.INTERNAL_SERVER_ERROR, "장바구니 삭제에 실패했습니다");
        }

        log.info("장바구니 삭제 완료: basketNo={}", basketNo);
    }

    @Override
    @Transactional
    public void deleteBaskets(String email, List<String> basketNos) {
        log.debug("장바구니 여러 개 삭제 시작: email={}, count={}", email, basketNos.size());

        if (basketNos == null || basketNos.isEmpty()) {
            throw new ApiException(ApiError.INVALID_PARAMETER, "삭제할 장바구니를 선택해주세요");
        }

        // 회원번호 조회
        String memberNo = getMemberNoByEmail(email);

        // 각 장바구니의 권한 확인
        for (String basketNo : basketNos) {
            BasketBase basketBase = basketBaseMapper.selectBasketBaseByBasketNo(basketNo);
            if (basketBase == null) {
                throw new ApiException(ApiError.DATA_NOT_FOUND,
                        String.format("장바구니를 찾을 수 없습니다: %s", basketNo));
            }

            if (!basketBase.getMemberNo().equals(memberNo)) {
                throw new ApiException(ApiError.FORBIDDEN, "본인의 장바구니만 삭제할 수 있습니다");
            }
        }

        // 장바구니 일괄 삭제
        int result = basketBaseTrxMapper.deleteBasketBaseByNos(basketNos);
        log.info("장바구니 여러 개 삭제 완료: email={}, deleted={}", email, result);
    }

    @Override
    @Transactional
    public void deleteAllBaskets(String email) {
        log.debug("장바구니 전체 삭제 시작: email={}", email);

        // 회원번호 조회
        String memberNo = getMemberNoByEmail(email);

        // 장바구니 전체 삭제
        int result = basketBaseTrxMapper.deleteBasketBaseByMemberNo(memberNo);
        log.info("장바구니 전체 삭제 완료: email={}, deleted={}", email, result);
    }

    /**
     * 이메일로 회원번호 조회
     *
     * @param email 이메일
     * @return 회원번호
     */
    private String getMemberNoByEmail(String email) {
        MemberBase member = memberBaseMapper.selectMemberBaseByEmail(email);
        if (member == null) {
            throw new ApiException(ApiError.DATA_NOT_FOUND, "회원 정보를 찾을 수 없습니다");
        }
        return member.getMemberNo();
    }
}
