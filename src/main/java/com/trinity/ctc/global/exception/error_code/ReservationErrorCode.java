package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {
    PAYING_BY_ANOTHER(HttpStatus.BAD_REQUEST, "다른 사용자가 결제중입니다."),
    ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "이미 예약된 좌석입니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 예약정보를 찾을 수 없습니다."),

    NOT_PREOCCUPIED(HttpStatus.BAD_REQUEST, "예약이 선점상태가 아닙니다."),
    ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 예약(결제)완료 된 예약입니다."),
    NOT_COMPLETED(HttpStatus.BAD_REQUEST, "예약이 완료상태가 아닙니다."),

    ALREADY_RESERVED_BY_USER(HttpStatus.BAD_REQUEST, "이미 선점, 완료 된 예약입니다."),

    RESERVATION_USER_MISMATCH(HttpStatus.BAD_REQUEST, "예약자와 요청 사용자가 다릅니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
