package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RestaurantErrorCode implements ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "식당을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
