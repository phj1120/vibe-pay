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
 * 토스페이먼츠 어댑터 테스트
 *
 * Given-When-Then 패턴을 사용하여 테스트 코드를 작성합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("토스페이먼츠 어댑터 테스트")
class TossAdapterTest {

    @Mock
    private PaymentInterfaceRequestLogMapper logMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TossAdapter tossAdapter;

    private PaymentInitiateRequest initiateRequest;
    private PaymentConfirmRequest confirmRequest;
    private PaymentCancelRequest cancelRequest;

    @BeforeEach
    void setUp() {
        // Given: 테스트에 사용할 기본 요청 객체 준비
        initiateRequest = new PaymentInitiateRequest();
        initiateRequest.setOrderId("ORDER_TEST_003");
        initiateRequest.setMemberId(3L);
        initiateRequest.setAmount(BigDecimal.valueOf(20000));
        initiateRequest.setProductName("토스 테스트 상품");
        initiateRequest.setPaymentMethod(PaymentMethod.CARD);
        initiateRequest.setPgCompany(PgCompany.TOSS);
        initiateRequest.setBuyerName("이토스");
        initiateRequest.setBuyerEmail("toss@example.com");
        initiateRequest.setBuyerPhone("01011112222");
        initiateRequest.setReturnUrl("http://localhost:3000/payment/toss/success");
        initiateRequest.setCancelUrl("http://localhost:3000/payment/toss/fail");

        confirmRequest = new PaymentConfirmRequest();
        confirmRequest.setOrderId("ORDER_TEST_003");
        confirmRequest.setMemberId(3L);
        confirmRequest.setAmount(BigDecimal.valueOf(20000));
        confirmRequest.setPaymentMethod(PaymentMethod.CARD);
        confirmRequest.setPgCompany(PgCompany.TOSS);
        confirmRequest.setPgTransactionId("TOSS_TXN_003");

        cancelRequest = new PaymentCancelRequest();
        cancelRequest.setOrderId("ORDER_TEST_003");
        cancelRequest.setPaymentId("PAYMENT_003");
        cancelRequest.setAmount(BigDecimal.valueOf(20000));
        cancelRequest.setPgCompany(PgCompany.TOSS);
        cancelRequest.setOriginalTransactionId("TOSS_TXN_003");
        cancelRequest.setOriginalApprovalNumber("TOSS_APPROVAL_003");
        cancelRequest.setCancelReason("상품 불량");
    }

    @Test
    @DisplayName("토스페이먼츠 PG사 지원 여부 확인")
    void supports_shouldReturnTrue_whenPgCompanyIsToss() {
        // Given: 토스페이먼츠 PG사

        // When: supports 메서드 호출
        boolean result = tossAdapter.supports(PgCompany.TOSS);

        // Then: true 반환
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 PG사는 지원하지 않음")
    void supports_shouldReturnFalse_whenPgCompanyIsNotToss() {
        // Given: 나이스페이 PG사

        // When: supports 메서드 호출
        boolean result = tossAdapter.supports(PgCompany.NICEPAY);

        // Then: false 반환
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("결제 초기화 성공 및 RESTful API 파라미터 확인")
    void initiate_shouldReturnSuccessResponseWithRestfulParameters_whenValidRequest() {
        // Given: 유효한 결제 초기화 요청

        // When: 결제 초기화 수행
        PaymentInitResponse response = tossAdapter.initiate(initiateRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPaymentUrl()).contains("tosspayments");
        assertThat(response.getMessage()).contains("TOSS");

        // Then: 토스페이먼츠 RESTful API 파라미터 확인
        Map<String, String> parameters = response.getParameters();
        assertThat(parameters).isNotNull();
        assertThat(parameters).containsKeys("clientKey", "orderId", "amount",
                "orderName", "customerName", "customerEmail", "successUrl", "failUrl");
        assertThat(parameters.get("orderId")).isEqualTo("ORDER_TEST_003");
        assertThat(parameters.get("amount")).isEqualTo("20000");
        assertThat(parameters.get("orderName")).isEqualTo("토스 테스트 상품");
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirm_shouldReturnSuccessResponse_whenValidRequest() {
        // Given: 유효한 결제 승인 요청

        // When: 결제 승인 수행
        PaymentConfirmResponse response = tossAdapter.confirm(confirmRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTransactionId()).contains("TOSS");
        assertThat(response.getApprovalNumber()).isNotEmpty();
        assertThat(response.getAmount()).isEqualTo(confirmRequest.getAmount());
        assertThat(response.getResultCode()).isEqualTo("DONE"); // 토스페이먼츠 성공 상태
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancel_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 결제 취소 요청
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // When: 결제 취소 수행
        PaymentCancelResponse response = tossAdapter.cancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).contains("TOSS_CANCEL");
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());
        assertThat(response.getResultCode()).isEqualTo("CANCELED"); // 토스페이먼츠 취소 상태

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
        PaymentCancelResponse response = tossAdapter.netCancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).contains("TOSS_NET_CANCEL");
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());

        // Then: 로그 기록 확인
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }

    @Test
    @DisplayName("토스페이먼츠는 RESTful API를 사용함")
    void toss_shouldUseRestfulApi() {
        // Given: 토스페이먼츠 어댑터

        // When: 결제 초기화
        PaymentInitResponse response = tossAdapter.initiate(initiateRequest);

        // Then: RESTful API 엔드포인트 확인
        assertThat(response.getPaymentUrl()).contains("api.tosspayments.com");
        assertThat(response.getPaymentUrl()).contains("/v1/");
    }
}
