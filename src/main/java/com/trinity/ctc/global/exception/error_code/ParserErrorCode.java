package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParserErrorCode implements ErrorCode {

    FAILED_PARSING(HttpStatus.BAD_REQUEST, "CSV 파싱 실패");

    private final HttpStatus httpStatus;
    private final String message;
}
