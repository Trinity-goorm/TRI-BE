package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements ErrorCode {

    EMPTY_SEARCH_RESULT(HttpStatus.BAD_REQUEST, "검색 결과가 없습니다."),
    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "검색어가 유효하지 않습니다."),
    NOT_FOUND_SEARCH_RESULT(HttpStatus.BAD_REQUEST, "검색 결과를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
