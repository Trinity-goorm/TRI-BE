package com.trinity.ctc.global.exception.error_code;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    EMPTY_CATEGORIES(HttpStatus.BAD_REQUEST, "카테고리가 비어있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
