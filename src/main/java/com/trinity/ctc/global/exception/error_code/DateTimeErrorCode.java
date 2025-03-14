package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DateTimeErrorCode implements ErrorCode {

    GIVEN_DATETIME_IS_PAST(HttpStatus.BAD_REQUEST, "주어진 시간은 과거입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
