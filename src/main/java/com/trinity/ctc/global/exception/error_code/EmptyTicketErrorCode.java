package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmptyTicketErrorCode implements ErrorCode {
    NEGATIVE_EMPTY_TICKET_COUNT(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "빈자리 알림 티켓 수가 0보다 적습니다."),
    NOT_ENOUGH_REMAINING_EMPTY_TICKETS(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "잔여 빈자리 알림 티켓이 부족합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}