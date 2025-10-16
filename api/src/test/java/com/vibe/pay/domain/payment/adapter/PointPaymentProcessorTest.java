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
 * 포인트 결제 프로세서 테스트
 *
 * Given-When-Then 패턴을 사용하여 테스트 코드를 작성합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("포인트 결제 프로세서 테스트")
class PointPaymentProcessorTest {

    @Mock
    private PaymentInterfaceRequestLogMapper logMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PointPaymentProcessor pointPaymentProcessor;

    private PaymentInitiateRequest initiateRequest;
    private PaymentConfirmRequest confirmRequest;
    private PaymentCancelRequest cancelRequest;

    @BeforeEach
    void setUp() {
        // Given: 테스트에 사용할 기본 요청 객체 준비
        initiateRequest = new PaymentInitiateRequest();
        initiateRequest.setOrderId("ORDER_TEST_004");
        initiateRequest.setMemberId(4L);
        initiateRequest.setAmount(BigDecimal.valueOf(5000));
        initiateRequest.setProductName("포인트 결제 테스트 상품");
        initiateRequest.setPaymentMethod(PaymentMethod.POINT);
        initiateRequest.setPgCompany(PgCompany.POINT);
        initiateRequest.setBuyerName("박포인트");
        initiateRequest.setBuyerEmail("point@example.com");
        initiateRequest.setBuyerPhone("01033334444");

        confirmRequest = new PaymentConfirmRequest();
        confirmRequest.setOrderId("ORDER_TEST_004");
        confirmRequest.setMemberId(4L);
        confirmRequest.setAmount(BigDecimal.valueOf(5000));
        confirmRequest.setPaymentMethod(PaymentMethod.POINT);
        confirmRequest.setPgCompany(PgCompany.POINT);

        cancelRequest = new PaymentCancelRequest();
        cancelRequest.setOrderId("ORDER_TEST_004");
        cancelRequest.setPaymentId("PAYMENT_004");
        cancelRequest.setAmount(BigDecimal.valueOf(5000));
        cancelRequest.setPgCompany(PgCompany.POINT);
        cancelRequest.setOriginalTransactionId("POINT_TXN_004");
        cancelRequest.setCancelReason("포인트 환급");
    }

    @Test
    @DisplayName("포인트 결제 방식 지원 여부 확인")
    void supports_shouldReturnTrue_whenPgCompanyIsPoint() {
        // Given: 포인트 결제

        // When: supports 메서드 호출
        boolean result = pointPaymentProcessor.supports(PgCompany.POINT);

        // Then: true 반환
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 PG사는 지원하지 않음")
    void supports_shouldReturnFalse_whenPgCompanyIsNotPoint() {
        // Given: 이니시스 PG사

        // When: supports 메서드 호출
        boolean result = pointPaymentProcessor.supports(PgCompany.INICIS);

        // Then: false 반환
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("포인트 결제 초기화 성공 및 사용 가능 포인트 확인")
    void initiate_shouldReturnSuccessResponseWithAvailablePoints_whenValidRequest() {
        // Given: 유효한 포인트 결제 초기화 요청

        // When: 결제 초기화 수행
        PaymentInitResponse response = pointPaymentProcessor.initiate(initiateRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPaymentUrl()).isEqualTo("INTERNAL_POINT_SYSTEM");
        assertThat(response.getMessage()).contains("Point");

        // Then: 포인트 관련 파라미터 확인
        Map<String, String> parameters = response.getParameters();
        assertThat(parameters).isNotNull();
        assertThat(parameters).containsKeys("memberId", "orderId", "amount", "availablePoints");
        assertThat(parameters.get("memberId")).isEqualTo("4");
        assertThat(parameters.get("amount")).isEqualTo("5000");
    }

    @Test
    @DisplayName("포인트 결제 승인 성공")
    void confirm_shouldReturnSuccessResponse_whenValidRequest() {
        // Given: 유효한 포인트 결제 승인 요청

        // When: 결제 승인 수행
        PaymentConfirmResponse response = pointPaymentProcessor.confirm(confirmRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTransactionId()).contains("POINT");
        assertThat(response.getApprovalNumber()).isNotEmpty();
        assertThat(response.getAmount()).isEqualTo(confirmRequest.getAmount());
        assertThat(response.getResultCode()).isEqualTo("0000");
    }

    @Test
    @DisplayName("포인트 환급(취소) 성공")
    void cancel_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 포인트 환급 요청
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // When: 포인트 환급 수행
        PaymentCancelResponse response = pointPaymentProcessor.cancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).contains("POINT_CANCEL");
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());

        // Then: 로그 기록 확인
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }

    @Test
    @DisplayName("포인트 망취소 성공")
    void netCancel_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 포인트 망취소 요청
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // When: 망취소 수행
        PaymentCancelResponse response = pointPaymentProcessor.netCancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).contains("POINT_NET_CANCEL");
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());

        // Then: 로그 기록 확인
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }

    @Test
    @DisplayName("포인트 결제는 외부 PG 연동 없이 내부 시스템 사용")
    void point_shouldUseInternalSystem() {
        // Given: 포인트 결제 프로세서

        // When: 결제 초기화
        PaymentInitResponse response = pointPaymentProcessor.initiate(initiateRequest);

        // Then: 내부 시스템 사용 확인
        assertThat(response.getPaymentUrl()).isEqualTo("INTERNAL_POINT_SYSTEM");
        assertThat(response.getPaymentUrl()).doesNotContain("http");
    }

    @Test
    @DisplayName("포인트 결제는 회원 ID가 필수")
    void point_shouldRequireMemberId() {
        // Given: 포인트 결제 초기화 요청

        // When: 결제 초기화
        PaymentInitResponse response = pointPaymentProcessor.initiate(initiateRequest);

        // Then: 회원 ID 포함 확인
        assertThat(response.getParameters()).containsKey("memberId");
        assertThat(response.getParameters().get("memberId")).isNotNull();
    }
}
