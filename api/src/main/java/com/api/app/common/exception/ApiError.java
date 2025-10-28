package com.api.app.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Getter
@AllArgsConstructor
public enum ApiError {

    // 성공
    SUCCESS("0000", "성공"),

    // 클라이언트 오류 (1xxx)
    EMPTY_PARAMETER("1001", "필수 파라미터가 누락되었습니다"),
    INVALID_PARAMETER("1002", "파라미터가 유효하지 않습니다"),
    DATA_NOT_FOUND("1003", "데이터가 존재하지 않습니다"),
    DUPLICATE_DATA("1004", "중복된 데이터가 존재합니다"),
    INVALID_FILE("1005", "유효하지 않은 파일입니다"),

    // 인증/인가 오류 (2xxx)
    UNAUTHORIZED("2001", "인증이 필요합니다"),
    INVALID_TOKEN("2002", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN("2003", "만료된 토큰입니다"),
    FORBIDDEN("2004", "접근 권한이 없습니다"),
    INVALID_CREDENTIALS("2005", "이메일 또는 비밀번호가 일치하지 않습니다"),
    DUPLICATE_EMAIL("2006", "이미 사용 중인 이메일입니다"),

    // 서버 오류 (9xxx)
    INTERNAL_SERVER_ERROR("9000", "서버 내부 오류가 발생했습니다"),
    VALIDATION_EXCEPTION("9100", "입력값 검증에 실패했습니다");

    private final String code;
    private final String message;
}
