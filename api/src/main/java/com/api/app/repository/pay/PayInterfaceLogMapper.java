package com.api.app.repository.pay;

import com.api.app.entity.PayInterfaceLog;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface PayInterfaceLogMapper {

    /**
     * 결제인터페이스로그 전체 조회
     *
     * @return 결제인터페이스로그 목록
     */
    List<PayInterfaceLog> selectAllPayInterfaceLog();

    /**
     * 결제인터페이스번호로 조회
     *
     * @param payInterfaceNo 결제인터페이스번호
     * @return 결제인터페이스로그 정보
     */
    PayInterfaceLog selectPayInterfaceLogByPayInterfaceNo(String payInterfaceNo);

    /**
     * 결제번호로 조회
     *
     * @param payNo 결제번호
     * @return 결제인터페이스로그 목록
     */
    List<PayInterfaceLog> selectPayInterfaceLogByPayNo(String payNo);

    /**
     * 회원번호로 조회
     *
     * @param memberNo 회원번호
     * @return 결제인터페이스로그 목록
     */
    List<PayInterfaceLog> selectPayInterfaceLogByMemberNo(String memberNo);
}
