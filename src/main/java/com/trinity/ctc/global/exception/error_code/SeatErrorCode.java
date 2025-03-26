package com.trinity.ctc.global.exception.error_code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SeatErrorCode implements ErrorCode {

    TIMESLOT_NOT_FOUND(HttpStatus.NOT_FOUND, "타임슬롯을 가져오지 못했습니다. DB를 확인하세요."),
    CAPACITY_IS_NEGATIVE(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "최소 인원수는 0보다 작을 수 없습니다."),
    BIGGER_MIN_CAPACITY(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "최소 인원수는 최대 인원수보다 클 수 없습니다."),
    NO_AVAILABLE_SEAT(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "예약가능 좌석이 0개입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 좌석을 찾을 수 없습니다."),
    ALREADY_EXIST_SEAT_AVAILABILITY(HttpStatus.BAD_REQUEST, "이미 존재하는 가용예약정보 데이터입니다."),

    REDIS_SYNC_FAILED(HttpStatus.CONFLICT, "Seat 레코드에 대한 Redis 연산 결과를 동기화하지 못했습니다.");



    private final HttpStatus httpStatus;
    private final String message;
}
