package com.vibe.pay.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 열거형
 *
 * 비즈니스 도메인별 에러 코드를 정의하고 HTTP 상태 코드와 매핑합니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-16
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러 (1000번대)
    SUCCESS("1000", "성공", HttpStatus.OK),
    INVALID_INPUT("1001", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("1002", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("1003", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR("1999", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 회원 도메인 (2000번대)
    MEMBER_NOT_FOUND("2001", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_EXISTS("2002", "이미 존재하는 회원입니다.", HttpStatus.CONFLICT),
    MEMBER_EMAIL_ALREADY_EXISTS("2003", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    MEMBER_INACTIVE("2004", "비활성화된 회원입니다.", HttpStatus.FORBIDDEN),
    MEMBER_DELETED("2005", "삭제된 회원입니다.", HttpStatus.GONE),

    // 상품 도메인 (3000번대)
    PRODUCT_NOT_FOUND("3001", "상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PRODUCT_OUT_OF_STOCK("3002", "상품의 재고가 부족합니다.", HttpStatus.CONFLICT),
    PRODUCT_ALREADY_EXISTS("3003", "이미 존재하는 상품입니다.", HttpStatus.CONFLICT),
    PRODUCT_INACTIVE("3004", "판매 중지된 상품입니다.", HttpStatus.FORBIDDEN),
    PRODUCT_INVALID_PRICE("3005", "상품 가격이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 주문 도메인 (4000번대)
    ORDER_NOT_FOUND("4001", "주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_CANCELLED("4002", "이미 취소된 주문입니다.", HttpStatus.CONFLICT),
    ORDER_CANNOT_CANCEL("4003", "취소할 수 없는 주문입니다.", HttpStatus.CONFLICT),
    ORDER_INVALID_STATUS("4004", "주문 상태가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    ORDER_INVALID_AMOUNT("4005", "주문 금액이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    ORDER_EMPTY_ITEMS("4006", "주문 상품이 비어있습니다.", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_NOT_FOUND("4007", "주문 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 결제 도메인 (5000번대)
    PAYMENT_NOT_FOUND("5001", "결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED("5002", "결제에 실패했습니다.", HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_ALREADY_COMPLETED("5003", "이미 완료된 결제입니다.", HttpStatus.CONFLICT),
    PAYMENT_ALREADY_CANCELLED("5004", "이미 취소된 결제입니다.", HttpStatus.CONFLICT),
    PAYMENT_AMOUNT_MISMATCH("5005", "결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_METHOD("5006", "유효하지 않은 결제 수단입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_TIMEOUT("5007", "결제 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT),

    // PG 연동 에러 (5100번대)
    PG_CONNECTION_ERROR("5101", "PG사 연동에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    PG_INVALID_RESPONSE("5102", "PG사 응답이 유효하지 않습니다.", HttpStatus.BAD_GATEWAY),
    PG_AUTHENTICATION_FAILED("5103", "PG사 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    PG_TRANSACTION_FAILED("5104", "PG사 거래가 실패했습니다.", HttpStatus.PAYMENT_REQUIRED),
    PG_CANCEL_FAILED("5105", "PG사 취소가 실패했습니다.", HttpStatus.CONFLICT),

    // 포인트 도메인 (6000번대)
    POINT_NOT_FOUND("6001", "포인트 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INSUFFICIENT_POINTS("6002", "포인트가 부족합니다.", HttpStatus.CONFLICT),
    POINT_TRANSACTION_FAILED("6003", "포인트 거래에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    POINT_INVALID_AMOUNT("6004", "포인트 금액이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    POINT_EXPIRED("6005", "포인트가 만료되었습니다.", HttpStatus.GONE),
    POINT_HISTORY_NOT_FOUND("6006", "포인트 이력을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
