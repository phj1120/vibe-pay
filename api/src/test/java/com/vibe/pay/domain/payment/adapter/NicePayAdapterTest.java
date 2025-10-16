package com.vibe.pay.domain.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.pay.domain.payment.dto.PaymentCancelRequest;
import com.vibe.pay.domain.payment.dto.PaymentCancelResponse;
import com.vibe.pay.domain.payment.dto.PaymentConfirmRequest;
import com.vibe.pay.domain.payment.dto.PaymentConfirmResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitResponse;
import com.vibe.pay.domain.payment.dto.PaymentInitiateRequest;
import com.vibe.pay.domain.payment.repository.PaymentInterfaceRequestLogMapper;
import com.vibe.pay.enums.PaymentMethod;
import com.vibe.pay.enums.PgCompany;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 나이스페이 어댑터 테스트
 *
 * Given-When-Then 패턴을 사용하여 테스트 코드를 작성합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("나이스페이 어댑터 테스트")
class NicePayAdapterTest {

    @Mock
    private PaymentInterfaceRequestLogMapper logMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NicePayAdapter nicePayAdapter;

    private PaymentInitiateRequest initiateRequest;
    private PaymentConfirmRequest confirmRequest;
    private PaymentCancelRequest cancelRequest;

    @BeforeEach
    void setUp() {
        // Given: 테스트에 사용할 기본 요청 객체 준비
        initiateRequest = new PaymentInitiateRequest();
        initiateRequest.setOrderId("ORDER_TEST_002");
        initiateRequest.setMemberId(2L);
        initiateRequest.setAmount(BigDecimal.valueOf(15000));
        initiateRequest.setProductName("나이스페이 테스트 상품");
        initiateRequest.setPaymentMethod(PaymentMethod.CARD);
        initiateRequest.setPgCompany(PgCompany.NICEPAY);
        initiateRequest.setBuyerName("김나이스");
        initiateRequest.setBuyerEmail("nicepay@example.com");
        initiateRequest.setBuyerPhone("01087654321");
        initiateRequest.setReturnUrl("http://localhost:3000/payment/nicepay/return");
        initiateRequest.setCancelUrl("http://localhost:3000/payment/nicepay/cancel");

        confirmRequest = new PaymentConfirmRequest();
        confirmRequest.setOrderId("ORDER_TEST_002");
        confirmRequest.setMemberId(2L);
        confirmRequest.setAmount(BigDecimal.valueOf(15000));
        confirmRequest.setPaymentMethod(PaymentMethod.CARD);
        confirmRequest.setPgCompany(PgCompany.NICEPAY);
        confirmRequest.setPgTransactionId("NICEPAY_TXN_002");

        cancelRequest = new PaymentCancelRequest();
        cancelRequest.setOrderId("ORDER_TEST_002");
        cancelRequest.setPaymentId("PAYMENT_002");
        cancelRequest.setAmount(BigDecimal.valueOf(15000));
        cancelRequest.setPgCompany(PgCompany.NICEPAY);
        cancelRequest.setOriginalTransactionId("NICEPAY_TXN_002");
        cancelRequest.setOriginalApprovalNumber("NICEPAY_APPROVAL_002");
        cancelRequest.setCancelReason("단순 변심");
    }

    @Test
    @DisplayName("나이스페이 PG사 지원 여부 확인")
    void supports_shouldReturnTrue_whenPgCompanyIsNicePay() {
        // Given: 나이스페이 PG사

        // When: supports 메서드 호출
        boolean result = nicePayAdapter.supports(PgCompany.NICEPAY);

        // Then: true 반환
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 PG사는 지원하지 않음")
    void supports_shouldReturnFalse_whenPgCompanyIsNotNicePay() {
        // Given: 이니시스 PG사

        // When: supports 메서드 호출
        boolean result = nicePayAdapter.supports(PgCompany.INICIS);

        // Then: false 반환
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("결제 초기화 성공 및 필수 파라미터 확인")
    void initiate_shouldReturnSuccessResponseWithParameters_whenValidRequest() {
        // Given: 유효한 결제 초기화 요청

        // When: 결제 초기화 수행
        PaymentInitResponse response = nicePayAdapter.initiate(initiateRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPaymentUrl()).contains("nicepay");
        assertThat(response.getMessage()).contains("NICEPAY");

        // Then: 나이스페이 필수 파라미터 확인
        Map<String, String> parameters = response.getParameters();
        assertThat(parameters).isNotNull();
        assertThat(parameters).containsKeys("mid", "moid", "amt", "goodName",
                "buyerName", "buyerEmail", "buyerTel", "ediDate", "SignData",
                "returnUrl", "cancelUrl", "version", "currency");
        assertThat(parameters.get("moid")).isEqualTo("ORDER_TEST_002");
        assertThat(parameters.get("amt")).isEqualTo("15000");
        assertThat(parameters.get("version")).isEqualTo("1.0");
        assertThat(parameters.get("currency")).isEqualTo("KRW");
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirm_shouldReturnSuccessResponse_whenValidRequest() {
        // Given: 유효한 결제 승인 요청

        // When: 결제 승인 수행
        PaymentConfirmResponse response = nicePayAdapter.confirm(confirmRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTransactionId()).contains("NICEPAY");
        assertThat(response.getApprovalNumber()).isNotEmpty();
        assertThat(response.getAmount()).isEqualTo(confirmRequest.getAmount());
        assertThat(response.getResultCode()).isEqualTo("3001"); // 나이스페이 성공 코드
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancel_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 결제 취소 요청
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // When: 결제 취소 수행
        PaymentCancelResponse response = nicePayAdapter.cancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).contains("NICEPAY_CANCEL");
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());
        assertThat(response.getResultCode()).isEqualTo("2001"); // 나이스페이 취소 성공 코드

        // Then: 로그 기록 확인
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }

    @Test
    @DisplayName("망취소 성공")
    void netCancel_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 망취소 요청
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // When: 망취소 수행
        PaymentCancelResponse response = nicePayAdapter.netCancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).contains("NICEPAY_NET_CANCEL");
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());

        // Then: 로그 기록 확인
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }
}
