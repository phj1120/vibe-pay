package com.vibe.pay.domain.payment.service;

import com.vibe.pay.domain.payment.entity.PaymentInterfaceRequestLog;
import com.vibe.pay.domain.payment.repository.PaymentInterfaceRequestLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 인터페이스 요청 로그 서비스
 * PG사 연동 시 요청/응답 로그를 기록하고 조회하는 비즈니스 로직을 처리하는 계층
 *
 * Technical Specification 참조:
 * - docs/v5/TechnicalSpecification/payment-log-spec.md
 *
 * @see PaymentInterfaceRequestLog
 * @see PaymentInterfaceRequestLogMapper
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentInterfaceRequestLogService {

    private final PaymentInterfaceRequestLogMapper logMapper;

    /**
     * PG 연동 요청/응답 로그 기록
     *
     * PG사로 보낸 요청과 받은 응답을 로그로 기록합니다.
     * 디버깅, 감사, 문제 해결을 위해 사용됩니다.
     *
     * @param paymentId 결제 ID
     * @param requestType 요청 타입 (INITIATE, CONFIRM, CANCEL, NET_CANCEL 등)
     * @param requestPayload 요청 페이로드 (JSON 또는 XML)
     * @param responsePayload 응답 페이로드 (JSON 또는 XML)
     * @throws RuntimeException 로그 기록 실패 시
     */
    @Transactional
    public void logRequest(String paymentId, String requestType,
                            String requestPayload, String responsePayload) {
        log.debug("Logging PG request: paymentId={}, requestType={}", paymentId, requestType);

        PaymentInterfaceRequestLog requestLog = new PaymentInterfaceRequestLog();
        requestLog.setPaymentId(paymentId);
        requestLog.setRequestType(requestType);
        requestLog.setRequestPayload(requestPayload);
        requestLog.setResponsePayload(responsePayload);
        requestLog.setRequestDate(LocalDateTime.now());

        logMapper.insert(requestLog);
        log.debug("PG request logged: logId={}", requestLog.getLogId());
    }

    /**
     * 결제 ID로 로그 조회
     *
     * @param paymentId 결제 ID
     * @return PG 연동 로그 엔티티 목록
     */
    public List<PaymentInterfaceRequestLog> getLogsByPaymentId(String paymentId) {
        log.debug("Fetching PG logs by paymentId: {}", paymentId);
        return logMapper.findByPaymentId(paymentId);
    }
}
