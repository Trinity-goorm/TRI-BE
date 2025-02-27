package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    SIGN_UP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 중 문제가 발생하였습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    SAME_PASSWORD_INPUT(HttpStatus.BAD_REQUEST, "기존 비밀번호와 동일합니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "사용자 인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다."),
    INVALID_CATEGORY_COUNT(HttpStatus.BAD_REQUEST , "사용자가 선택한 카테고리가 3개가 아닙니다."),
    NOT_TEMPORAL_USER(HttpStatus.FORBIDDEN, "임시 사용자가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
