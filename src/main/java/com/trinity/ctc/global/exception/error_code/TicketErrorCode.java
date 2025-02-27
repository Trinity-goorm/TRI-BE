package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TicketErrorCode implements ErrorCode {
    NEGATIVE_TICKET_COUNT(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "티켓 수가 0보다 적습니다."),
    NOT_ENOUGH_REMAINING_TICKETS(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "잔여 티켓이 부족합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
