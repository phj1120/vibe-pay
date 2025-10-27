package com.api.app.repository.pay;

import com.api.app.entity.PayBase;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface PayBaseTrxMapper {

    /**
     * 결제번호 시퀀스 생성
     *
     * @return 생성된 결제번호
     */
    String generatePayNo();

    /**
     * 결제 등록
     *
     * @param payBase 결제 정보
     * @return 등록된 건수
     */
    int insertPayBase(PayBase payBase);

    /**
     * 결제 수정
     *
     * @param payBase 결제 정보
     * @return 수정된 건수
     */
    int updatePayBase(PayBase payBase);

    /**
     * 결제 삭제
     *
     * @param payNo 결제번호
     * @return 삭제된 건수
     */
    int deletePayBase(String payNo);
}
