package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatTypeErrorCode implements ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 좌석타입을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
