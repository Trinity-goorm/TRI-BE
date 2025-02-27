package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorCode implements ErrorCode {
    INVALID_TOKEN_CATEGORY(HttpStatus.BAD_REQUEST, "토큰의 카테고리가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰 값이 올바르지 않습니다."),
    REFRESH_TOKEN_IS_NULL(HttpStatus.BAD_REQUEST, "리프레시 토큰 값이 NULL 입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "리프레시 토큰이 만료되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
