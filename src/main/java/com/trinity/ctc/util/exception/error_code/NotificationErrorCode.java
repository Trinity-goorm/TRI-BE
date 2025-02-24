package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    ALREADY_SUBSCRIBED(HttpStatus.CONFLICT , "이미 신청한 빈자리 알림 건입니다."),
    NO_SUBSCRIPTION(HttpStatus.NOT_FOUND, "빈자리 알림 신청건이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
