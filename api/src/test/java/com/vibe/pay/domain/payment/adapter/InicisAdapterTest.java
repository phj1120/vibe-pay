package com.vibe.pay.domain.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe.pay.common.exception.PaymentException;
import com.vibe.pay.common.util.WebClientUtil;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 이니시스 어댑터 테스트
 *
 * Given-When-Then 패턴을 사용하여 테스트 코드를 작성합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("이니시스 어댑터 테스트")
class InicisAdapterTest {

    @Mock
    private PaymentInterfaceRequestLogMapper logMapper;

    @Mock
    private WebClientUtil webClientUtil;

    @InjectMocks
    private InicisAdapter inicisAdapter;

    private ObjectMapper objectMapper;

    private PaymentInitiateRequest initiateRequest;
    private PaymentConfirmRequest confirmRequest;
    private PaymentCancelRequest cancelRequest;

    @BeforeEach
    void setUp() {
        // ObjectMapper 실제 인스턴스 생성
        objectMapper = new ObjectMapper();
        ReflectionTestUtils.setField(inicisAdapter, "objectMapper", objectMapper);

        // @Value 필드 설정 (ReflectionTestUtils 사용)
        ReflectionTestUtils.setField(inicisAdapter, "mid", "INIpayTest");
        ReflectionTestUtils.setField(inicisAdapter, "apiKey", "TEST_API_KEY");
        ReflectionTestUtils.setField(inicisAdapter, "signKey", "TEST_SIGN_KEY");
        ReflectionTestUtils.setField(inicisAdapter, "testApiUrl", "https://stgstdpay.inicis.com");
        // Given: 테스트에 사용할 기본 요청 객체 준비
        initiateRequest = new PaymentInitiateRequest();
        initiateRequest.setOrderId("ORDER_TEST_001");
        initiateRequest.setMemberId(1L);
        initiateRequest.setAmount(BigDecimal.valueOf(10000));
        initiateRequest.setProductName("테스트 상품");
        initiateRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        initiateRequest.setPgCompany(PgCompany.INICIS);
        initiateRequest.setBuyerName("홍길동");
        initiateRequest.setBuyerEmail("test@example.com");
        initiateRequest.setBuyerPhone("01012345678");
        initiateRequest.setReturnUrl("http://localhost:3000/payment/return");
        initiateRequest.setCancelUrl("http://localhost:3000/payment/cancel");

        confirmRequest = new PaymentConfirmRequest();
        confirmRequest.setOrderId("ORDER_TEST_001");
        confirmRequest.setMemberId(1L);
        confirmRequest.setAmount(BigDecimal.valueOf(10000));
        confirmRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        confirmRequest.setPgCompany(PgCompany.INICIS);
        confirmRequest.setPgTransactionId("INICIS_TXN_001");
        confirmRequest.setAuthToken("TEST_AUTH_TOKEN");
        confirmRequest.setAuthUrl("https://stgstdpay.inicis.com/stdpay/web/INIStdPayAppr.ini");
        confirmRequest.setMid("INIpayTest");

        cancelRequest = new PaymentCancelRequest();
        cancelRequest.setOrderId("ORDER_TEST_001");
        cancelRequest.setPaymentId("PAYMENT_001");
        cancelRequest.setAmount(BigDecimal.valueOf(10000));
        cancelRequest.setPgCompany(PgCompany.INICIS);
        cancelRequest.setOriginalTransactionId("INICIS_TXN_001");
        cancelRequest.setOriginalApprovalNumber("INICIS_APPROVAL_001");
        cancelRequest.setCancelReason("고객 변심");
        cancelRequest.setAuthToken("TEST_AUTH_TOKEN");
        cancelRequest.setNetCancelUrl("https://stgstdpay.inicis.com/stdpay/web/INIStdPayNetCancel.ini");
        cancelRequest.setMid("INIpayTest");
    }

    @Test
    @DisplayName("이니시스 PG사 지원 여부 확인")
    void supports_shouldReturnTrue_whenPgCompanyIsInicis() {
        // Given: 이니시스 PG사

        // When: supports 메서드 호출
        boolean result = inicisAdapter.supports(PgCompany.INICIS);

        // Then: true 반환
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 PG사는 지원하지 않음")
    void supports_shouldReturnFalse_whenPgCompanyIsNotInicis() {
        // Given: 나이스페이 PG사

        // When: supports 메서드 호출
        boolean result = inicisAdapter.supports(PgCompany.NICEPAY);

        // Then: false 반환
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("결제 초기화 성공")
    void initiate_shouldReturnSuccessResponse_whenValidRequest() {
        // Given: 유효한 결제 초기화 요청

        // When: 결제 초기화 수행
        PaymentInitResponse response = inicisAdapter.initiate(initiateRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getPaymentUrl()).isNotEmpty();
        assertThat(response.getPaymentUrl()).contains("stdpay/pay.ini");
        assertThat(response.getMessage()).contains("INICIS");

        // Then: 결제창 파라미터 검증
        Map<String, String> parameters = response.getParameters();
        assertThat(parameters).isNotNull();
        assertThat(parameters).containsKeys("mid", "oid", "price", "goodName", "buyerName",
                "buyerTel", "buyerEmail", "timestamp", "mKey", "signature", "verification");
        assertThat(parameters.get("mid")).isEqualTo("INIpayTest");
        assertThat(parameters.get("oid")).isEqualTo("ORDER_TEST_001");
        assertThat(parameters.get("price")).isEqualTo("10000");
        assertThat(parameters.get("goodName")).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirm_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 결제 승인 요청 및 Mock 설정
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // 이니시스 승인 API 응답 Mock
        String mockResponse = """
                {
                    "resultCode": "0000",
                    "resultMsg": "결제 성공",
                    "tid": "INIpayTest_20251017_123456",
                    "TotPrice": 10000,
                    "applNum": "12345678",
                    "CARD_Num": "************1234",
                    "CARD_IssuerName": "국민카드"
                }
                """;
        when(webClientUtil.postFormUrlEncoded(anyString(), anyMap())).thenReturn(mockResponse);

        // When: 결제 승인 수행
        PaymentConfirmResponse response = inicisAdapter.confirm(confirmRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getTransactionId()).isEqualTo("INIpayTest_20251017_123456");
        assertThat(response.getApprovalNumber()).isEqualTo("12345678");
        assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(response.getResultCode()).isEqualTo("0000");
        assertThat(response.getMaskedCardNumber()).isEqualTo("************1234");
        assertThat(response.getCardCompany()).isEqualTo("국민카드");

        // Then: API 호출 및 로그 기록 확인
        verify(webClientUtil, times(1)).postFormUrlEncoded(anyString(), anyMap());
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancel_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 결제 취소 요청
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // When: 결제 취소 수행
        PaymentCancelResponse response = inicisAdapter.cancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).isNotEmpty();
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());
        assertThat(response.getOriginalTransactionId()).isEqualTo(cancelRequest.getOriginalTransactionId());

        // Then: 로그 기록 확인
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }

    @Test
    @DisplayName("망취소 성공")
    void netCancel_shouldReturnSuccessResponse_whenValidRequest() throws Exception {
        // Given: 유효한 망취소 요청
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // When: 망취소 수행
        PaymentCancelResponse response = inicisAdapter.netCancel(cancelRequest);

        // Then: 성공 응답 반환
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCancelTransactionId()).isNotEmpty();
        assertThat(response.getCancelAmount()).isEqualTo(cancelRequest.getAmount());

        // Then: 로그 기록 확인
        verify(logMapper, times(1)).insert(any());
        verify(logMapper, times(1)).updateResponse(any());
    }

    @Test
    @DisplayName("결제 승인 실패 - resultCode가 0000이 아님")
    void confirm_shouldThrowException_whenResultCodeIsNotSuccess() throws Exception {
        // Given: 실패 응답을 반환하는 Mock 설정
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        String mockResponse = """
                {
                    "resultCode": "1001",
                    "resultMsg": "카드 승인 거절",
                    "tid": "INIpayTest_20251017_123456"
                }
                """;
        when(webClientUtil.postFormUrlEncoded(anyString(), anyMap())).thenReturn(mockResponse);

        // When & Then: PaymentException 발생
        PaymentException exception = assertThrows(PaymentException.class, () -> {
            inicisAdapter.confirm(confirmRequest);
        });

        // Then: 예외 메시지 검증
        assertThat(exception.getPgErrorCode()).isEqualTo("1001");
        assertThat(exception.getPgErrorMessage()).isEqualTo("카드 승인 거절");
    }

    @Test
    @DisplayName("결제 승인 실패 - 금액 불일치")
    void confirm_shouldThrowException_whenAmountMismatch() throws Exception {
        // Given: 금액이 다른 응답을 반환하는 Mock 설정
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        String mockResponse = """
                {
                    "resultCode": "0000",
                    "resultMsg": "결제 성공",
                    "tid": "INIpayTest_20251017_123456",
                    "TotPrice": 5000,
                    "applNum": "12345678"
                }
                """;
        when(webClientUtil.postFormUrlEncoded(anyString(), anyMap())).thenReturn(mockResponse);

        // When & Then: PaymentException 발생
        PaymentException exception = assertThrows(PaymentException.class, () -> {
            inicisAdapter.confirm(confirmRequest);
        });

        // Then: 예외 메시지 검증
        assertThat(exception.getPgErrorCode()).isEqualTo("AMOUNT_MISMATCH");
    }

    @Test
    @DisplayName("결제 취소 시 금액 불일치 시 예외 발생")
    void cancel_shouldThrowException_whenAmountMismatch() throws Exception {
        // Given: 금액이 불일치하는 결제 취소 요청
        doNothing().when(logMapper).insert(any());
        doNothing().when(logMapper).updateResponse(any());

        // 취소 금액을 다르게 설정
        cancelRequest.setAmount(BigDecimal.valueOf(5000));

        // When & Then: 예외 발생
        assertThrows(RuntimeException.class, () -> {
            inicisAdapter.cancel(cancelRequest);
        });
    }
}
