package com.trinity.ctc.util.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatErrorCode implements ErrorCode {

    TIMESLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "타임슬롯을 가져오지 못했습니다. DB를 확인하세요."),
    CAPACITY_IS_NEGATIVE(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "최소 인원수는 0보다 작을 수 없습니다."),
    BIGGER_MIN_CAPACITY(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "최소 인원수는 최대 인원수보다 클 수 없습니다."),
    NO_AVAILABLE_SEAT(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "예약가능 좌석이 0개입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
