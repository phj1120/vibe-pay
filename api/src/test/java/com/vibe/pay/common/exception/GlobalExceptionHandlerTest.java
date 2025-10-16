package com.vibe.pay.common.exception;

import com.vibe.pay.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GlobalExceptionHandler 테스트
 *
 * @author system
 * @version 1.0
 * @since 2025-10-16
 */
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("BusinessException 처리 테스트")
    void handleBusinessException() {
        // given
        ErrorCode errorCode = ErrorCode.MEMBER_NOT_FOUND;
        BusinessException exception = new BusinessException(errorCode);

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(errorCode.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(response.getBody().getErrorCode()).isEqualTo(errorCode.getCode());
    }

    @Test
    @DisplayName("BusinessException 상세 메시지 포함 처리 테스트")
    void handleBusinessExceptionWithDetailMessage() {
        // given
        ErrorCode errorCode = ErrorCode.MEMBER_NOT_FOUND;
        String detailMessage = "회원 ID: 12345";
        BusinessException exception = new BusinessException(errorCode, detailMessage);

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(errorCode.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains(errorCode.getMessage());
        assertThat(response.getBody().getMessage()).contains(detailMessage);
    }

    @Test
    @DisplayName("PaymentException 처리 테스트")
    void handlePaymentException() {
        // given
        ErrorCode errorCode = ErrorCode.PAYMENT_FAILED;
        String pgCompany = "TOSS";
        String pgTransactionId = "TXN123456";
        PaymentException exception = new PaymentException(errorCode, pgCompany, pgTransactionId);

        // when
        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handlePaymentException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(errorCode.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo(errorCode.getMessage());
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().get("pgCompany")).isEqualTo(pgCompany);
        assertThat(response.getBody().getData().get("pgTransactionId")).isEqualTo(pgTransactionId);
    }

    @Test
    @DisplayName("PaymentException PG 에러 정보 포함 처리 테스트")
    void handlePaymentExceptionWithPgError() {
        // given
        ErrorCode errorCode = ErrorCode.PG_TRANSACTION_FAILED;
        String pgCompany = "TOSS";
        String pgTransactionId = "TXN123456";
        String pgErrorCode = "PG_ERR_001";
        String pgErrorMessage = "PG사 거래 실패";
        PaymentException exception = new PaymentException(
            errorCode, pgCompany, pgTransactionId, pgErrorCode, pgErrorMessage
        );

        // when
        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handlePaymentException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(errorCode.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().get("pgErrorCode")).isEqualTo(pgErrorCode);
    }

    @Test
    @DisplayName("ValidationException 처리 테스트")
    void handleValidationException() {
        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("email", "이메일 형식이 올바르지 않습니다.");
        fieldErrors.put("password", "비밀번호는 8자 이상이어야 합니다.");
        ValidationException exception = new ValidationException(errorCode, fieldErrors);

        // when
        ResponseEntity<ApiResponse<Map<String, String>>> response = handler.handleValidationException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(errorCode.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData().get("email")).isEqualTo("이메일 형식이 올바르지 않습니다.");
        assertThat(response.getBody().getData().get("password")).isEqualTo("비밀번호는 8자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리 테스트")
    void handleMethodArgumentNotValidException() {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("memberRequest", "email", "이메일은 필수입니다.");
        FieldError fieldError2 = new FieldError("memberRequest", "name", "이름은 필수입니다.");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // when
        ResponseEntity<ApiResponse<Map<String, String>>> response =
            handler.handleMethodArgumentNotValidException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData().get("email")).isEqualTo("이메일은 필수입니다.");
        assertThat(response.getBody().getData().get("name")).isEqualTo("이름은 필수입니다.");
    }

    @Test
    @DisplayName("IllegalArgumentException 처리 테스트")
    void handleIllegalArgumentException() {
        // given
        String errorMessage = "잘못된 인자입니다.";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgumentException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains(errorMessage);
    }

    @Test
    @DisplayName("IllegalStateException 처리 테스트")
    void handleIllegalStateException() {
        // given
        String errorMessage = "잘못된 상태입니다.";
        IllegalStateException exception = new IllegalStateException(errorMessage);

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalStateException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains(errorMessage);
    }

    @Test
    @DisplayName("RuntimeException 처리 테스트")
    void handleRuntimeException() {
        // given
        RuntimeException exception = new RuntimeException("예상치 못한 오류");

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleRuntimeException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }

    @Test
    @DisplayName("Exception 처리 테스트 - 최종 fallback")
    void handleException() {
        // given
        Exception exception = new Exception("예상치 못한 예외");

        // when
        ResponseEntity<ApiResponse<Void>> response = handler.handleException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    }
}
