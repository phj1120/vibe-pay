package com.api.app.repository.pay;

import com.api.app.entity.PayInterfaceLog;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface PayInterfaceLogTrxMapper {

    /**
     * 결제인터페이스번호 시퀀스 생성
     *
     * @return 생성된 결제인터페이스번호
     */
    String generatePayInterfaceNo();

    /**
     * 결제인터페이스로그 등록
     *
     * @param payInterfaceLog 결제인터페이스로그 정보
     * @return 등록된 건수
     */
    int insertPayInterfaceLog(PayInterfaceLog payInterfaceLog);

    /**
     * 결제인터페이스로그 수정
     *
     * @param payInterfaceLog 결제인터페이스로그 정보
     * @return 수정된 건수
     */
    int updatePayInterfaceLog(PayInterfaceLog payInterfaceLog);

    /**
     * 결제인터페이스로그 삭제
     *
     * @param payInterfaceNo 결제인터페이스번호
     * @return 삭제된 건수
     */
    int deletePayInterfaceLog(String payInterfaceNo);
}
