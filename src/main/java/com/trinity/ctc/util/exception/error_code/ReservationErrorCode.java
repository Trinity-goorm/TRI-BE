package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    PAYING_BY_ANOTHER(HttpStatus.BAD_REQUEST, "다른 사용자가 결제중입니다."),
    ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "이미 예약된 좌석입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
